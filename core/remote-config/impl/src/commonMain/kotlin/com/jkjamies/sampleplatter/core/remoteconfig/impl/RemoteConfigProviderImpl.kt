package com.jkjamies.sampleplatter.core.remoteconfig.impl

import com.jkjamies.sampleplatter.core.remoteconfig.FeatureFlag
import com.jkjamies.sampleplatter.core.remoteconfig.RemoteConfig
import com.jkjamies.sampleplatter.core.remoteconfig.RemoteConfigProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RemoteConfigProviderImpl(
    private val source: RemoteConfigSource,
) : RemoteConfigProvider {

    override fun isEnabled(flag: FeatureFlag): Boolean = source.isEnabled(flag)

    override fun observe(flag: FeatureFlag): Flow<Boolean> = source.observe(flag)

    override fun <T> getConfig(config: RemoteConfig<T>): T = source.getConfig(config)

    override fun <T> observeConfig(config: RemoteConfig<T>): Flow<T> = source.observeConfig(config)
}
