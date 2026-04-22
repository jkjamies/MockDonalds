package com.mockdonalds.app.core.featureflag

interface FeatureFlagDefinition {
    val flag: FeatureFlag
    val description: String
    val owner: String
    val lifecycle: FlagLifecycle
}

enum class FlagLifecycle { Experiment, KillSwitch, Ops, Permanent }
