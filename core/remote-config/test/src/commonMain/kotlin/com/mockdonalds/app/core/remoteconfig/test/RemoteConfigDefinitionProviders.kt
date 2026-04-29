package com.mockdonalds.app.core.remoteconfig.test

import com.mockdonalds.app.core.remoteconfig.RemoteConfigDefinition
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface RemoteConfigDefinitionProviders {
    @Multibinds(allowEmpty = true)
    fun remoteConfigDefinitions(): Set<RemoteConfigDefinition<*>>
}
