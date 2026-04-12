package com.mockdonalds.app.core.featureflag.impl

import com.mockdonalds.app.core.featureflag.FeatureFlag
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class DefaultRemoteFeatureFlagSource : RemoteFeatureFlagSource {
    override fun isEnabled(flag: FeatureFlag): Boolean = flag.defaultValue
    override fun observe(flag: FeatureFlag): Flow<Boolean> = flowOf(flag.defaultValue)
}
