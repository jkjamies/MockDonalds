package com.jkjamies.sampleplatter.core.analytics

interface AnalyticsEvent {
    val name: String
    val properties: Map<String, Any> get() = emptyMap()
}
