package com.mockdonalds.app.core.featureflag.impl

import com.mockdonalds.app.core.featureflag.FeatureFlag
import com.mockdonalds.app.core.featureflag.FeatureFlagProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class FeatureFlagProviderImpl(
    private val remoteSource: RemoteFeatureFlagSource,
) : FeatureFlagProvider {

    override fun isEnabled(flag: FeatureFlag): Boolean {
        return remoteSource.isEnabled(flag)
    }

    override fun observe(flag: FeatureFlag): Flow<Boolean> {
        return remoteSource.observe(flag)
    }
}
