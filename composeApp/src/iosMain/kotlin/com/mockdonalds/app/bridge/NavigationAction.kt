package com.mockdonalds.app.bridge

import com.slack.circuit.runtime.screen.Screen

/**
 * Navigation commands emitted by [BridgeNavigator] and consumed by SwiftUI's CircuitNavigator.
 *
 * Actions are batched into lists before delivery — multiple actions from a single synchronous
 * event handler (e.g. `pop()` + `goTo()`) arrive as one batch, processed in one SwiftUI update
 * cycle to avoid visual artifacts.
 */
sealed class NavigationAction {
    data class GoTo(val screen: Screen) : NavigationAction()
    data object Pop : NavigationAction()
    data class ResetRoot(val screen: Screen) : NavigationAction()
    data class SwitchTab(val tag: String) : NavigationAction()
    data class DeepLink(val screens: List<Screen>) : NavigationAction()
}
