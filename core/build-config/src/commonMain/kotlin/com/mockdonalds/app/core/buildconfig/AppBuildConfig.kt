package com.mockdonalds.app.core.buildconfig

interface AppBuildConfig {
    val appName: String
    val market: String
    val env: String
    val baseUrl: String
    val cdnUrl: String
    val locale: String
    val currency: String
}
