package com.mockdonalds.app.core.analytics

interface AnalyticsEvent {
    val name: String
    val properties: Map<String, Any> get() = emptyMap()
}
