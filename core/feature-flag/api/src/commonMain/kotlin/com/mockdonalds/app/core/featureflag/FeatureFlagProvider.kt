package com.mockdonalds.app.core.featureflag

import kotlinx.coroutines.flow.Flow

interface FeatureFlagProvider {
    fun isEnabled(flag: FeatureFlag): Boolean
    fun observe(flag: FeatureFlag): Flow<Boolean>
}
