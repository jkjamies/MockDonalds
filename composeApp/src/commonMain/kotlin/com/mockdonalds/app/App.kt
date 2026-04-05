package com.mockdonalds.app

import androidx.compose.runtime.Composable
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.splash.api.navigation.SplashScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dev.zacsweers.metro.createGraph

@Composable
fun MockDonaldsApp() {
    val graph = createGraph<AppGraph>()

    MockDonaldsTheme {
        val backStack = rememberSaveableBackStack(root = SplashScreen)
        val navigator = rememberCircuitNavigator(backStack) { /* root pop - no-op */ }

        CircuitCompositionLocals(graph.circuit) {
            NavigableCircuitContent(
                navigator = navigator,
                backStack = backStack,
            )
        }
    }
}
