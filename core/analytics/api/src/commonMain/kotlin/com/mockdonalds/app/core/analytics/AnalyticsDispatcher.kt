package com.mockdonalds.app.core.analytics

interface AnalyticsDispatcher {
    fun track(event: AnalyticsEvent)
    fun trackScreenView(screenName: String)
}
