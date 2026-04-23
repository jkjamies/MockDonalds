package com.mockdonalds.app.core.buildconfig

import com.mockdonalds.app.core.buildconfig.BuildConfigField.Group

interface AppBuildConfig {
    @DebugConfigField(Group.Identity) val appName: String
    @DebugConfigField(Group.Identity) val appId: String
    @DebugConfigField(Group.Identity) val market: String
    @DebugConfigField(Group.Identity) val env: String
    @DebugConfigField(Group.Identity) val buildType: String
    @DebugConfigField(Group.Urls) val baseUrl: String
    @DebugConfigField(Group.Urls) val cdnUrl: String
    @DebugConfigField(Group.Urls) val menuBaseUrl: String
    @DebugConfigField(Group.Urls) val orderBaseUrl: String
    @DebugConfigField(Group.Urls) val accountBaseUrl: String
    @DebugConfigField(Group.Urls) val rewardsBaseUrl: String
    @DebugConfigField(Group.Urls) val storeBaseUrl: String
    @DebugConfigField(Group.Localization) val locale: String
    @DebugConfigField(Group.Localization) val currency: String
}

val AppBuildConfig.isDebug: Boolean get() = buildType == "debug"
