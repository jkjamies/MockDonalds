package com.jkjamies.sampleplatter.core.buildconfig

import com.jkjamies.sampleplatter.core.buildconfig.BuildConfigField.Group

interface AppBuildConfig {
    @DebugConfigField(Group.Identity)
    val appName: String

    @DebugConfigField(Group.Identity)
    val appId: String

    @DebugConfigField(Group.Identity)
    val market: String

    @DebugConfigField(Group.Identity)
    val env: String

    @DebugConfigField(Group.Identity)
    val buildType: String

    @DebugConfigField(Group.Urls)
    val baseUrl: String

    @DebugConfigField(Group.Urls)
    val cdnUrl: String

    @DebugConfigField(Group.Urls)
    val menuBaseUrl: String

    @DebugConfigField(Group.Urls)
    val orderBaseUrl: String

    @DebugConfigField(Group.Urls)
    val accountBaseUrl: String

    @DebugConfigField(Group.Urls)
    val rewardsBaseUrl: String

    @DebugConfigField(Group.Urls)
    val storeBaseUrl: String

    @DebugConfigField(Group.Urls)
    val nutritionUrl: String

    @DebugConfigField(Group.Localization)
    val locale: String

    @DebugConfigField(Group.Localization)
    val currency: String

    /**
     * Spoonacular API key for the order feature's reference integration.
     *
     * Sourced from `local.properties` (gitignored) in this public reference repo so the key
     * never lands in version control. In an enterprise / internal build, move this to per-env
     * `core/build-config/markets/{market}/{env}.properties` so it's CI-managed and rotatable
     * per environment.
     *
     * Tagged `Group.Secrets` so the KSP registry can enumerate it but the debug-config viewer
     * (BuildConfigDebugPresenter) filters it out — surfacing API keys in any user-facing
     * debug menu is a leak risk.
     */
    @DebugConfigField(Group.Secrets)
    val spoonacularApiKey: String
}

val AppBuildConfig.isDebug: Boolean get() = buildType == "debug"
