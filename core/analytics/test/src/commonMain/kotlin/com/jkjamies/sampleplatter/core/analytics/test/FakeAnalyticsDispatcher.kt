package com.jkjamies.sampleplatter.core.analytics.test

import com.jkjamies.sampleplatter.core.analytics.AnalyticsDispatcher
import com.jkjamies.sampleplatter.core.analytics.AnalyticsEvent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class FakeAnalyticsDispatcher : AnalyticsDispatcher {

    private val _trackedEvents = mutableListOf<AnalyticsEvent>()
    val trackedEvents: List<AnalyticsEvent> get() = _trackedEvents

    private val _trackedScreenViews = mutableListOf<String>()
    val trackedScreenViews: List<String> get() = _trackedScreenViews

    override fun track(event: AnalyticsEvent) {
        _trackedEvents.add(event)
    }

    override fun trackScreenView(screenName: String) {
        _trackedScreenViews.add(screenName)
    }

    fun reset() {
        _trackedEvents.clear()
        _trackedScreenViews.clear()
    }
}
