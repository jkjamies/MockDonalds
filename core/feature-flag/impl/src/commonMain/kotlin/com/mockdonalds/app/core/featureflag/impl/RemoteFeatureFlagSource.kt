package com.mockdonalds.app.core.featureflag.impl

import com.mockdonalds.app.core.featureflag.FeatureFlag
import kotlinx.coroutines.flow.Flow

interface RemoteFeatureFlagSource {
    fun isEnabled(flag: FeatureFlag): Boolean
    fun observe(flag: FeatureFlag): Flow<Boolean>
}
