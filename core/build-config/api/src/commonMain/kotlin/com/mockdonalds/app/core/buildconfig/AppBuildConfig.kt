package com.mockdonalds.app.core.buildconfig

interface AppBuildConfig {
    val appName: String
    val appId: String
    val market: String
    val env: String
    val buildType: String
    val baseUrl: String
    val cdnUrl: String
    val menuBaseUrl: String
    val orderBaseUrl: String
    val accountBaseUrl: String
    val rewardsBaseUrl: String
    val storeBaseUrl: String
    val locale: String
    val currency: String
}

val AppBuildConfig.isDebug: Boolean get() = buildType == "debug"
