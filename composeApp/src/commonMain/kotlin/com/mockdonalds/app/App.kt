package com.mockdonalds.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.mockdonalds.app.features.order.api.navigation.OrderScreen
import com.mockdonalds.app.features.rewards.api.navigation.RewardsScreen
import com.mockdonalds.app.features.scan.api.navigation.ScanScreen
import com.mockdonalds.app.features.login.api.navigation.LoginScreen
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dev.zacsweers.metro.createGraph

@Composable
fun MockDonaldsApp() {
    val graph = createGraph<AppGraph>()

    MockDonaldsTheme {
        val backStack = rememberSaveableBackStack(root = HomeScreen)
        val navigator = rememberCircuitNavigator(backStack) { /* root pop - no-op */ }

        CircuitCompositionLocals(graph.circuit) {
            val topScreen = backStack.lastOrNull()?.screen

            val currentRoute = when (topScreen) {
                is HomeScreen -> "home"
                is OrderScreen -> "order"
                is RewardsScreen -> "rewards"
                is ScanScreen -> "scan"
                is MoreScreen -> "more"
                is LoginScreen -> "login"
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
                                    "login" -> LoginScreen
                                    else -> return@MockDonaldsBottomNavigation
                                }
                                navigator.resetRoot(targetScreen)
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize() /* Padding intentionally ignored as bottom nav is overlay */) {
                    NavigableCircuitContent(
                        navigator = navigator,
                        backStack = backStack,
                    )
                }
            }
        }
    }
}
