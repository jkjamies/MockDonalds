package com.jkjamies.sampleplatter.bridge

import com.jkjamies.sampleplatter.ProdAppGraph
import com.jkjamies.sampleplatter.core.circuit.TabScreen
import com.jkjamies.sampleplatter.core.remoteconfig.HarnessIosBridge
import com.jkjamies.sampleplatter.core.network.AkamaiSensorBridge
import com.jkjamies.sampleplatter.features.home.api.navigation.HomeScreen
import com.jkjamies.sampleplatter.features.login.api.navigation.LoginScreen
import com.jkjamies.sampleplatter.navigation.AnalyticsNavigationListener
import com.jkjamies.sampleplatter.navigation.AuthInterceptor
import com.jkjamies.sampleplatter.navigation.InterceptingNavigator
import com.jkjamies.sampleplatter.navigation.createDeepLinkParser
import com.jkjamies.sampleplatter.navigation.findTabByTag
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.zacsweers.metro.createGraphFactory

class IosApp(
    harnessIosBridge: HarnessIosBridge,
    akamaiSensorBridge: AkamaiSensorBridge,
) {
    private val graph = createGraphFactory<ProdAppGraph.Factory>()
        .create(harnessIosBridge, akamaiSensorBridge)
        .also { it.loggerInitializer.initialize() }

    val circuit: Circuit get() = graph.circuit

    val market: String get() = graph.appBuildConfig.market
    val env: String get() = graph.appBuildConfig.env

    private val analyticsListener = AnalyticsNavigationListener(graph.analyticsDispatcher)

    private val bridgeNavigator = BridgeNavigator(
        onSwitchTab = { tag ->
            findTabByTag(tag)?.let { analyticsListener.onResetRoot(it) }
        },
    )

    private val interceptingNavigator = InterceptingNavigator(
        delegate = bridgeNavigator,
        interceptors = listOf(
            AuthInterceptor(graph.authManager) { returnTo ->
                LoginScreen(returnTo = returnTo)
            },
        ),
        listeners = listOf(analyticsListener),
    )

    init {
        analyticsListener.onResetRoot(HomeScreen)
    }

    val navigator: BridgeNavigator get() = bridgeNavigator

    private val deepLinkParser = createDeepLinkParser()

    fun presenterBridge(screen: Screen): CircuitPresenterKotlinBridge<CircuitUiState> {
        val presenter = circuit.presenter(screen, interceptingNavigator)
            ?: error("No presenter found for screen: $screen")
        @Suppress("UNCHECKED_CAST")
        return CircuitPresenterKotlinBridge(
            presenter = presenter as Presenter<CircuitUiState>,
        )
    }

    fun deepLink(uri: String) {
        val screens = deepLinkParser.parse(uri) ?: return
        val intercepted = interceptingNavigator.deepLink(screens)
        if (intercepted.isEmpty()) return

        val first = intercepted.first()
        if (first is TabScreen) {
            // First screen is a tab — switch to it, push the rest
            bridgeNavigator.switchTab(first.tag)
            intercepted.drop(1).forEach { bridgeNavigator.goTo(it) }
        } else {
            // No tab root — push all screens onto current tab
            intercepted.forEach { bridgeNavigator.goTo(it) }
        }
        // Track only the final destination — intermediate screens aren't viewed.
        // Note: switchTab sets suppressTabCallback=true, so the onSwitchTab callback
        // does NOT fire here — manual tracking below is the only tracking for deep links.
        // If suppressTabCallback is ever removed, this will double-track tab deep links.
        if (intercepted.size == 1) {
            analyticsListener.onResetRoot(intercepted.first())
        } else {
            analyticsListener.onGoTo(intercepted.last())
        }
    }
}
