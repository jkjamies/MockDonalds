package com.mockdonalds.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.mockdonalds.app.features.scan.api.navigation.ScanScreen
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import dev.zacsweers.metro.createGraph

@Composable
fun MockDonaldsApp(windowSizeClass: WindowSizeClass) {
    val graph = createGraph<AppGraph>()

    CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
    MockDonaldsTheme {
        val backStack = rememberSaveableBackStack(root = HomeScreen)
        val navigator = rememberCircuitNavigator(
            backStack = backStack,
            onRootPop = { },
        )

        CircuitCompositionLocals(graph.circuit) {
            val topScreen = backStack.topRecord?.screen

            val currentRoute = when (topScreen) {
                is HomeScreen -> "home"
                is OrderScreen -> "order"
                is RewardsScreen -> "rewards"
                is ScanScreen -> "scan"
                is MoreScreen -> "more"
                else -> ""
            }

            Scaffold(
                bottomBar = {
                    if (currentRoute.isNotEmpty()) {
                        MockDonaldsBottomNavigation(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                val targetScreen = when (route) {
                                    "home" -> HomeScreen
                                    "order" -> OrderScreen
                                    "rewards" -> RewardsScreen
                                    "scan" -> ScanScreen
                                    "more" -> MoreScreen
                                    else -> return@MockDonaldsBottomNavigation
                                }
                                navigator.resetRoot(targetScreen)
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
                }
            }
        }
    }
    }
}
