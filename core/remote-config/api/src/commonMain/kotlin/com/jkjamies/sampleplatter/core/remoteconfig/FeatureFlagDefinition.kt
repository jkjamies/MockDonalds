package com.jkjamies.sampleplatter.core.remoteconfig

interface FeatureFlagDefinition {
    val flag: FeatureFlag
    val description: String
    val owner: String
    val lifecycle: FlagLifecycle
}

enum class FlagLifecycle { Experiment, KillSwitch, Ops, Permanent }
