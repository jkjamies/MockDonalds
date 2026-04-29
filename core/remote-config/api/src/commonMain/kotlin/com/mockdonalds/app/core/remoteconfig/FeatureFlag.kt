package com.mockdonalds.app.core.remoteconfig

data class FeatureFlag(
    val key: String,
    val defaultValue: Boolean,
)
