package com.jkjamies.sampleplatter.navigation

import com.slack.circuit.runtime.screen.Screen

interface NavigationEventListener {
    fun onGoTo(screen: Screen) {}
    fun onResetRoot(screen: Screen) {}
}
