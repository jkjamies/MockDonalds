package com.mockdonalds.app.navint

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.screen.Screen

/**
 * Sets up a full Circuit navigation environment for navint tests.
 * Uses the real DI graph (NavIntAppGraph) with real presenters and fake data layer.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun ComposeContentTestRule.setNavIntContent(
    circuit: Circuit,
    root: Screen,
    onNavigator: ((com.slack.circuit.runtime.Navigator) -> Unit)? = null,
) {
    setContent {
        val size = DpSize(400.dp, 800.dp)
        CompositionLocalProvider(
            LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
        ) {
            MockDonaldsTheme {
                val backStack = rememberSaveableBackStack(root = root)
                val navigator = rememberCircuitNavigator(
                    backStack = backStack,
                    onRootPop = {},
                )

                onNavigator?.invoke(navigator)

                CircuitCompositionLocals(circuit) {
                    NavigableCircuitContent(
                        navigator = navigator,
                        backStack = backStack,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
