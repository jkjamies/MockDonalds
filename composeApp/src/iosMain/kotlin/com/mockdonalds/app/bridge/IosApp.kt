package com.mockdonalds.app.bridge

import com.mockdonalds.app.ProdAppGraph
import com.mockdonalds.app.core.circuit.TabScreen
import com.mockdonalds.app.features.login.api.navigation.LoginScreen
import com.mockdonalds.app.navigation.AuthInterceptor
import com.mockdonalds.app.navigation.InterceptingNavigator
import com.mockdonalds.app.navigation.createDeepLinkParser
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dev.zacsweers.metro.createGraph

class IosApp {
    private val graph = createGraph<ProdAppGraph>()

    val circuit: Circuit get() = graph.circuit

    val market: String get() = graph.appBuildConfig.market
    val env: String get() = graph.appBuildConfig.env

    private val bridgeNavigator = BridgeNavigator()

    private val interceptingNavigator = InterceptingNavigator(
        delegate = bridgeNavigator,
        interceptors = listOf(
            AuthInterceptor(graph.authManager) { returnTo ->
                LoginScreen(returnTo = returnTo)
            },
        ),
    )

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
    }
}
