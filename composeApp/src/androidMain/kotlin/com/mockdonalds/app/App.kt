package com.mockdonalds.app

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.core.circuit.TabScreen
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.mockdonalds.app.features.login.api.navigation.LoginScreen
import com.mockdonalds.app.navigation.AuthInterceptor
import com.mockdonalds.app.navigation.InterceptingNavigator
import com.mockdonalds.app.navigation.createDeepLinkParser
import com.mockdonalds.app.navigation.findTabByTag
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import dev.zacsweers.metro.createGraph

@Composable
fun MockDonaldsApp(windowSizeClass: WindowSizeClass, deepLinkIntent: Intent? = null) {
    val graph = remember { createGraph<ProdAppGraph>() }

    val deepLinkParser = remember { createDeepLinkParser() }

    CompositionLocalProvider(
        LocalWindowSizeClass provides windowSizeClass,
    ) {
    MockDonaldsTheme {
        val backStack = rememberSaveableBackStack(root = HomeScreen)
        val circuitNavigator = rememberCircuitNavigator(
            backStack = backStack,
            onRootPop = { },
        )
        val navigator = remember(circuitNavigator) {
            InterceptingNavigator(
                delegate = circuitNavigator,
                interceptors = listOf(
                    AuthInterceptor(graph.authManager) { returnTo ->
                        LoginScreen(returnTo = returnTo)
                    },
                ),
            )
        }

        LaunchedEffect(deepLinkIntent) {
            val uri = deepLinkIntent?.data?.toString() ?: return@LaunchedEffect
            val screens = deepLinkParser.parse(uri) ?: return@LaunchedEffect
            val intercepted = navigator.deepLink(screens)
            if (intercepted.isNotEmpty()) {
                circuitNavigator.resetRoot(intercepted.first())
                intercepted.drop(1).forEach { navigator.goTo(it) }
            }
        }

        CircuitCompositionLocals(graph.circuit) {
            val topScreen = backStack.topRecord?.screen

            val currentRoute = (topScreen as? TabScreen)?.tag ?: ""

            Scaffold(
                bottomBar = {
                    if (currentRoute.isNotEmpty()) {
                        MockDonaldsBottomNavigation(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                val target = findTabByTag(route)
                                    ?: return@MockDonaldsBottomNavigation
                                navigator.resetRoot(target)
                            },
                        )
                    }
                },
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    NavigableCircuitContent(
                        navigator = navigator,
                        backStack = backStack,
                        decoratorFactory = remember(navigator) {
                            GestureNavigationDecorationFactory(
                                onBackInvoked = navigator::pop,
                            )
                        },
                    )
                    if (currentRoute.isNotEmpty()) {
                        Text(
                            text = "${graph.appBuildConfig.market.uppercase()}/${graph.appBuildConfig.env}",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding(),
                        )
                    }
                }
            }

        }
    }
    }
}
