package com.mockdonalds.app.core.featureflag.impl

import com.mockdonalds.app.core.featureflag.FeatureFlag
import com.mockdonalds.app.core.featureflag.FeatureFlagProvider
import com.mockdonalds.app.core.featureflag.ObserveFeatureFlag
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class ObserveFeatureFlagImpl(
    private val provider: FeatureFlagProvider,
) : ObserveFeatureFlag() {
    override fun createObservable(params: FeatureFlag): Flow<Boolean> =
        provider.observe(params)
}
