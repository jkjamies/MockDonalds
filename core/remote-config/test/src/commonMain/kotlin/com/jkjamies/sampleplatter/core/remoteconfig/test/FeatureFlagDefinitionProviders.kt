package com.jkjamies.sampleplatter.core.remoteconfig.test

import com.jkjamies.sampleplatter.core.remoteconfig.FeatureFlagDefinition
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

// Parallel to core:remote-config:impl/FeatureFlagDefinitionProviders. Metro consolidates
// multibind slots by type+scope, so test graphs that pull this module (without impl)
// still see the Set<FeatureFlagDefinition> slot.
@ContributesTo(AppScope::class)
interface FeatureFlagDefinitionProviders {
    @Multibinds(allowEmpty = true)
    fun featureFlagDefinitions(): Set<FeatureFlagDefinition>
}
