package com.mockdonalds.app.core.remoteconfig.test

import com.mockdonalds.app.core.remoteconfig.FeatureFlag
import com.mockdonalds.app.core.remoteconfig.RemoteConfig
import com.mockdonalds.app.core.remoteconfig.RemoteConfigProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class FakeRemoteConfigProvider : RemoteConfigProvider {

    private val flags = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val configs = MutableStateFlow<Map<String, Any?>>(emptyMap())

    override fun isEnabled(flag: FeatureFlag): Boolean =
        flags.value[flag.key] ?: flag.defaultValue

    override fun observe(flag: FeatureFlag): Flow<Boolean> =
        flags.map { it[flag.key] ?: flag.defaultValue }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getConfig(config: RemoteConfig<T>): T =
        (configs.value[config.key] as T?) ?: config.default

    @Suppress("UNCHECKED_CAST")
    override fun <T> observeConfig(config: RemoteConfig<T>): Flow<T> =
        configs.map { (it[config.key] as T?) ?: config.default }

    fun setEnabled(flag: FeatureFlag, enabled: Boolean) {
        flags.value = flags.value + (flag.key to enabled)
    }

    fun <T> setConfig(config: RemoteConfig<T>, value: T) {
        configs.value = configs.value + (config.key to value)
    }

    fun reset() {
        flags.value = emptyMap()
        configs.value = emptyMap()
    }
}
