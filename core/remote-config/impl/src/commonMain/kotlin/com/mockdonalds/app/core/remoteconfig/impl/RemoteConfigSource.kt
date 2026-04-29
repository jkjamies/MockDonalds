package com.mockdonalds.app.core.remoteconfig.impl

import com.mockdonalds.app.core.remoteconfig.FeatureFlag
import com.mockdonalds.app.core.remoteconfig.RemoteConfig
import kotlinx.coroutines.flow.Flow

interface RemoteConfigSource {
    fun isEnabled(flag: FeatureFlag): Boolean
    fun observe(flag: FeatureFlag): Flow<Boolean>
    fun <T> getConfig(config: RemoteConfig<T>): T
    fun <T> observeConfig(config: RemoteConfig<T>): Flow<T>
}
