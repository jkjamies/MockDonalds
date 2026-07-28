package com.jkjamies.sampleplatter.core.remoteconfig.impl

import com.jkjamies.sampleplatter.core.remoteconfig.FeatureFlagDefinition
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface FeatureFlagDefinitionProviders {
    @Multibinds(allowEmpty = true)
    fun featureFlagDefinitions(): Set<FeatureFlagDefinition>
}
