package com.mockdonalds.app.core.featureflag.impl

import com.mockdonalds.app.core.featureflag.FeatureFlagDefinition
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface FeatureFlagDefinitionProviders {
    @Multibinds(allowEmpty = true)
    fun featureFlagDefinitions(): Set<FeatureFlagDefinition>
}
