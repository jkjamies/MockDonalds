package com.mockdonalds.app.core.featureflag.impl

import com.mockdonalds.app.core.featureflag.FeatureFlag
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class HarnessRemoteFeatureFlagSourceImpl(
    private val bridge: HarnessIosBridge,
) : RemoteFeatureFlagSource {

    override fun isEnabled(flag: FeatureFlag): Boolean =
        bridge.boolVariation(flag.key, flag.defaultValue)

    override fun observe(flag: FeatureFlag): Flow<Boolean> = callbackFlow {
        val handle = bridge.registerListener(flag.key) { value ->
            trySend(value)
        }
        awaitClose { handle.cancel() }
    }.onStart { emit(bridge.boolVariation(flag.key, flag.defaultValue)) }
        .distinctUntilChanged()
}
