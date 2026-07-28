package com.jkjamies.sampleplatter.core.analytics

interface AnalyticsDispatcher {
    fun track(event: AnalyticsEvent)
    fun trackScreenView(screenName: String)
}
