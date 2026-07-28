package com.jkjamies.sampleplatter.core.analytics.test

import com.jkjamies.sampleplatter.core.analytics.AnalyticsEvent
import com.jkjamies.sampleplatter.core.analytics.TrackAnalyticsEvent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class FakeTrackAnalyticsEvent : TrackAnalyticsEvent() {

    private val _trackedEvents = mutableListOf<AnalyticsEvent>()
    val trackedEvents: List<AnalyticsEvent> get() = _trackedEvents

    override suspend fun doWork(params: AnalyticsEvent) {
        _trackedEvents.add(params)
    }

    fun reset() {
        _trackedEvents.clear()
    }
}
