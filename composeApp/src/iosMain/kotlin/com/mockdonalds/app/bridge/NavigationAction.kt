package com.mockdonalds.app.bridge

import com.slack.circuit.runtime.screen.Screen

sealed class NavigationAction {
    data object Idle : NavigationAction()
    data class GoTo(val screen: Screen) : NavigationAction()
    data object Pop : NavigationAction()
    data class ResetRoot(val screen: Screen) : NavigationAction()
    data class DeepLink(val screens: List<Screen>) : NavigationAction()
}
