package com.mockdonalds.app.core.remoteconfig

import kotlinx.coroutines.flow.Flow

interface RemoteConfigProvider {
    fun isEnabled(flag: FeatureFlag): Boolean
    fun observe(flag: FeatureFlag): Flow<Boolean>
    fun <T> getConfig(config: RemoteConfig<T>): T
    fun <T> observeConfig(config: RemoteConfig<T>): Flow<T>
}
