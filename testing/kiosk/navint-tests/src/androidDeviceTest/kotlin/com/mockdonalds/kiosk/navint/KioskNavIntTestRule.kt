package com.mockdonalds.kiosk.navint

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen

/**
 * Sets up a kiosk navint Compose environment. Mirrors the consumer
 * `setNavIntContent` rule but sized for the kiosk hardware (vertical
 * 32" portrait, expanded width-class).
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun ComposeContentTestRule.setKioskNavIntContent(
    circuit: Circuit,
    root: Screen,
    onNavigator: ((Navigator) -> Unit)? = null,
) {
    setContent {
        // 1080×1920 kiosk-portrait approximation in Compose dp.
        val size = DpSize(540.dp, 960.dp)
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
