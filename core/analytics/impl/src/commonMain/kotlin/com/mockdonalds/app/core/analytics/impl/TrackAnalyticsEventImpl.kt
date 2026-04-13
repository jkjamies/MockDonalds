package com.mockdonalds.app.core.analytics.impl

import com.mockdonalds.app.core.analytics.AnalyticsDispatcher
import com.mockdonalds.app.core.analytics.AnalyticsEvent
import com.mockdonalds.app.core.analytics.TrackAnalyticsEvent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
class TrackAnalyticsEventImpl @Inject constructor(
    private val analyticsDispatcher: AnalyticsDispatcher,
) : TrackAnalyticsEvent() {
    override suspend fun doWork(params: AnalyticsEvent) {
        analyticsDispatcher.track(params)
    }
}
