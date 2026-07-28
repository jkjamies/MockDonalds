package com.jkjamies.sampleplatter.features.more.presentation

import com.jkjamies.sampleplatter.features.more.api.navigation.MoreTabExtension
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface MoreTabExtensionProviders {
    @Multibinds(allowEmpty = true)
    fun moreTabExtensions(): Set<MoreTabExtension>
}
