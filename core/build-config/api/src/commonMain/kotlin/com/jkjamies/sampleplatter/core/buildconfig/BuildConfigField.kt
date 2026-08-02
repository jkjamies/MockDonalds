package com.jkjamies.sampleplatter.core.buildconfig

data class BuildConfigField(
    val name: String,
    val value: String,
    val group: Group,
) {
    enum class Group {
        Identity,
        Urls,
        Localization,

        // Filtered out of the user-facing debug menu in BuildConfigDebugPresenter — surfacing
        // API keys and similar credentials in any debug surface is a leak risk.
        Secrets,
    }
}

fun AppBuildConfig.asFields(): List<BuildConfigField> = generatedFields()
