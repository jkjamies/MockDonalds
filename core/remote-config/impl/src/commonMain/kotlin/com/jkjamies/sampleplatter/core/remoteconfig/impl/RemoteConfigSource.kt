package com.jkjamies.sampleplatter.core.remoteconfig.impl

import com.jkjamies.sampleplatter.core.remoteconfig.FeatureFlag
import com.jkjamies.sampleplatter.core.remoteconfig.RemoteConfig
import kotlinx.coroutines.flow.Flow

interface RemoteConfigSource {
    fun isEnabled(flag: FeatureFlag): Boolean
    fun observe(flag: FeatureFlag): Flow<Boolean>
    fun <T> getConfig(config: RemoteConfig<T>): T
    fun <T> observeConfig(config: RemoteConfig<T>): Flow<T>
}
