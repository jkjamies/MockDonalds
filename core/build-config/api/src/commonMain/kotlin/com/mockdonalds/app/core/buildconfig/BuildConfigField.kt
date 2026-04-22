package com.mockdonalds.app.core.buildconfig

data class BuildConfigField(
    val name: String,
    val value: String,
    val group: Group,
) {
    enum class Group { Identity, Urls, Localization }
}

fun AppBuildConfig.asFields(): List<BuildConfigField> = listOf(
    BuildConfigField("appName", appName, BuildConfigField.Group.Identity),
    BuildConfigField("appId", appId, BuildConfigField.Group.Identity),
    BuildConfigField("market", market, BuildConfigField.Group.Identity),
    BuildConfigField("env", env, BuildConfigField.Group.Identity),
    BuildConfigField("buildType", buildType, BuildConfigField.Group.Identity),
    BuildConfigField("baseUrl", baseUrl, BuildConfigField.Group.Urls),
    BuildConfigField("cdnUrl", cdnUrl, BuildConfigField.Group.Urls),
    BuildConfigField("menuBaseUrl", menuBaseUrl, BuildConfigField.Group.Urls),
    BuildConfigField("orderBaseUrl", orderBaseUrl, BuildConfigField.Group.Urls),
    BuildConfigField("accountBaseUrl", accountBaseUrl, BuildConfigField.Group.Urls),
    BuildConfigField("rewardsBaseUrl", rewardsBaseUrl, BuildConfigField.Group.Urls),
    BuildConfigField("storeBaseUrl", storeBaseUrl, BuildConfigField.Group.Urls),
    BuildConfigField("locale", locale, BuildConfigField.Group.Localization),
    BuildConfigField("currency", currency, BuildConfigField.Group.Localization),
)
