package com.mockdonalds.app.core.remoteconfig.impl

import com.mockdonalds.app.core.remoteconfig.FeatureFlag
import com.mockdonalds.app.core.remoteconfig.RemoteConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class HarnessRemoteConfigSourceImpl(
    private val bridge: HarnessIosBridge,
) : RemoteConfigSource {

    private val json = Json { ignoreUnknownKeys = true }

    override fun isEnabled(flag: FeatureFlag): Boolean =
        bridge.boolVariation(flag.key, flag.defaultValue)

    override fun observe(flag: FeatureFlag): Flow<Boolean> = callbackFlow {
        val handle = bridge.registerListener(flag.key) {
            trySend(bridge.boolVariation(flag.key, flag.defaultValue))
        }
        awaitClose { handle.cancel() }
    }.onStart { emit(bridge.boolVariation(flag.key, flag.defaultValue)) }
        .distinctUntilChanged()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getConfig(config: RemoteConfig<T>): T = when (config) {
        is RemoteConfig.StringConfig -> bridge.stringVariation(config.key, config.default)
        is RemoteConfig.LongConfig   -> bridge.longVariation(config.key, config.default)
        is RemoteConfig.DoubleConfig -> bridge.doubleVariation(config.key, config.default)
        is RemoteConfig.JsonConfig<*> -> decodeJson(config as RemoteConfig.JsonConfig<T>)
    } as T

    override fun <T> observeConfig(config: RemoteConfig<T>): Flow<T> = callbackFlow {
        val handle = bridge.registerListener(config.key) {
            trySend(getConfig(config))
        }
        awaitClose { handle.cancel() }
    }.onStart { emit(getConfig(config)) }
        .distinctUntilChanged()

    private fun <T> decodeJson(config: RemoteConfig.JsonConfig<T>): T {
        val defaultJson = json.encodeToString(config.serializer, config.default)
        val raw = bridge.jsonVariation(config.key, defaultJson)
        return runCatching { json.decodeFromString(config.serializer, raw) }
            .getOrDefault(config.default)
    }
}
