package com.mockdonalds.app.core.buildconfig

data class BuildConfigField(
    val name: String,
    val value: String,
    val group: Group,
) {
    enum class Group { Identity, Urls, Localization }
}

fun AppBuildConfig.asFields(): List<BuildConfigField> = generatedFields()
