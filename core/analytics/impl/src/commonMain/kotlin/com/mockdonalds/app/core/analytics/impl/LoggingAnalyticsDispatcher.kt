package com.mockdonalds.app.core.analytics.impl

import com.mockdonalds.app.core.analytics.AnalyticsDispatcher
import com.mockdonalds.app.core.analytics.AnalyticsEvent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
class LoggingAnalyticsDispatcher @Inject constructor() : AnalyticsDispatcher {
    override fun track(event: AnalyticsEvent) {
        println("[Analytics] track: ${event.name} ${event.properties}")
    }

    override fun trackScreenView(screenName: String) {
        println("[Analytics] screenView: $screenName")
    }
}
