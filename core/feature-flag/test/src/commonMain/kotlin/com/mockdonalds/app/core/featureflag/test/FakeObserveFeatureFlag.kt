package com.mockdonalds.app.core.featureflag.test

import com.mockdonalds.app.core.featureflag.FeatureFlag
import com.mockdonalds.app.core.featureflag.ObserveFeatureFlag
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class FakeObserveFeatureFlag @Inject constructor() : ObserveFeatureFlag() {

    private val flags = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    override fun createObservable(params: FeatureFlag): Flow<Boolean> =
        flags.map { it[params.key] ?: params.defaultValue }

    fun setEnabled(flag: FeatureFlag, enabled: Boolean) {
        flags.value = flags.value + (flag.key to enabled)
    }

    fun reset() {
        flags.value = emptyMap()
    }
}
