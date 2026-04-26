package com.mockdonalds.app.core.featureflag.impl

import android.app.Application
import com.mockdonalds.app.core.featureflag.FeatureFlag
import com.mockdonalds.app.core.featureflag.FeatureFlagBuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.events.EvaluationListener
import io.harness.cfsdk.cloud.model.Target
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class HarnessRemoteFeatureFlagSourceImpl(
    application: Application,
) : RemoteFeatureFlagSource {

    private val client: CfClient = CfClient.getInstance().also { instance ->
        instance.initialize(
            application,
            FeatureFlagBuildConfig.HARNESS_CLIENT_ID,
            CfConfiguration.builder().build(),
            Target().identifier("anonymous"),
        )
    }

    override fun isEnabled(flag: FeatureFlag): Boolean =
        client.boolVariation(flag.key, flag.defaultValue)

    override fun observe(flag: FeatureFlag): Flow<Boolean> = callbackFlow {
        val listener = EvaluationListener {
            trySend(client.boolVariation(flag.key, flag.defaultValue))
        }
        client.registerEvaluationListener(flag.key, listener)
        awaitClose { client.unregisterEvaluationListener(flag.key, listener) }
    }.onStart { emit(client.boolVariation(flag.key, flag.defaultValue)) }
}
