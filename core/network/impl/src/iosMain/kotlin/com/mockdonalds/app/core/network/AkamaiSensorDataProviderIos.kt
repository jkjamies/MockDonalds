package com.mockdonalds.app.core.network

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class AkamaiSensorDataProviderIos(
    private val bridge: AkamaiSensorBridge,
) : SensorDataProvider {
    override suspend fun currentSensorData(): String = bridge.currentSensorData()
}
