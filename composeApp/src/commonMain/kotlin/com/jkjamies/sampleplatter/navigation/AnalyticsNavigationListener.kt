package com.jkjamies.sampleplatter.navigation

import com.jkjamies.sampleplatter.core.analytics.AnalyticsDispatcher
import com.slack.circuit.runtime.screen.Screen

class AnalyticsNavigationListener(
    private val analyticsDispatcher: AnalyticsDispatcher,
) : NavigationEventListener {
    override fun onGoTo(screen: Screen) {
        analyticsDispatcher.trackScreenView(screen::class.simpleName ?: "Unknown")
    }

    override fun onResetRoot(screen: Screen) {
        analyticsDispatcher.trackScreenView(screen::class.simpleName ?: "Unknown")
    }

}
