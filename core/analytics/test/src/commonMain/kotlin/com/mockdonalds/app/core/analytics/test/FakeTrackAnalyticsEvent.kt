package com.mockdonalds.app.core.analytics.test

import com.mockdonalds.app.core.analytics.AnalyticsEvent
import com.mockdonalds.app.core.analytics.TrackAnalyticsEvent
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
