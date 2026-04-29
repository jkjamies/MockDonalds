package com.mockdonalds.app.core.remoteconfig.impl

import android.app.Application
import com.mockdonalds.app.core.logger.featureLogger
import com.mockdonalds.app.core.remoteconfig.FeatureFlag
import com.mockdonalds.app.core.remoteconfig.RemoteConfig
import com.mockdonalds.app.core.remoteconfig.RemoteConfigBuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.events.AuthCallback
import io.harness.cfsdk.cloud.events.AuthResult
import io.harness.cfsdk.cloud.events.EvaluationListener
import io.harness.cfsdk.cloud.model.AuthInfo
import io.harness.cfsdk.cloud.model.Target
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class HarnessRemoteConfigSourceImpl(
    application: Application,
) : RemoteConfigSource {

    private val log = featureLogger("HarnessRemoteConfig")

    // Init is async on Android; variation reads return defaults until the SDK lands real
    // values, and EvaluationListener flows re-emit on first real value (eventual consistency).
    // The AuthCallback is observability only — surfaces init success/failure so a misconfigured
    // client ID or auth issue is loud in logs instead of silently serving defaults forever.
    private val client: CfClient = CfClient.getInstance().also { instance ->
        instance.initialize(
            application,
            RemoteConfigBuildConfig.HARNESS_CLIENT_ID,
            CfConfiguration.builder().build(),
            Target().identifier("anonymous"),
            object : AuthCallback {
                override fun authorizationSuccess(authInfo: AuthInfo?, result: AuthResult?) {
                    if (result?.isSuccess == true) {
                        log.i { "Harness initialized successfully" }
                    } else {
                        log.e(throwable = result?.error) { "Harness init failed — serving defaults" }
                    }
                }
            },
        )
    }

    private val json = Json { ignoreUnknownKeys = true }

    override fun isEnabled(flag: FeatureFlag): Boolean =
        client.boolVariation(flag.key, flag.defaultValue)

    override fun observe(flag: FeatureFlag): Flow<Boolean> = callbackFlow {
        val listener = EvaluationListener {
            trySend(client.boolVariation(flag.key, flag.defaultValue))
        }
        client.registerEvaluationListener(flag.key, listener)
        awaitClose { client.unregisterEvaluationListener(flag.key, listener) }
    }.onStart { emit(client.boolVariation(flag.key, flag.defaultValue)) }
        .distinctUntilChanged()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getConfig(config: RemoteConfig<T>): T = when (config) {
        is RemoteConfig.StringConfig -> client.stringVariation(config.key, config.default)
        is RemoteConfig.LongConfig   -> client.stringVariation(config.key, config.default.toString()).toLongOrNull() ?: config.default
        is RemoteConfig.DoubleConfig -> client.numberVariation(config.key, config.default)
        is RemoteConfig.JsonConfig<*> -> decodeJson(config as RemoteConfig.JsonConfig<T>)
    } as T

    override fun <T> observeConfig(config: RemoteConfig<T>): Flow<T> = callbackFlow {
        val listener = EvaluationListener {
            trySend(getConfig(config))
        }
        client.registerEvaluationListener(config.key, listener)
        awaitClose { client.unregisterEvaluationListener(config.key, listener) }
    }.onStart { emit(getConfig(config)) }
        .distinctUntilChanged()

    private fun <T> decodeJson(config: RemoteConfig.JsonConfig<T>): T {
        val defaultJson = json.encodeToString(config.serializer, config.default)
        val raw = client.stringVariation(config.key, defaultJson)
        return runCatching { json.decodeFromString(config.serializer, raw) }
            .getOrDefault(config.default)
    }
}
