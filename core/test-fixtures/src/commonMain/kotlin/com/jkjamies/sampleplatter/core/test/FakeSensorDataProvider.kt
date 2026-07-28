package com.jkjamies.sampleplatter.core.test

import com.jkjamies.sampleplatter.core.network.SensorDataProvider

class FakeSensorDataProvider(
    var data: String = "",
) : SensorDataProvider {
    override suspend fun currentSensorData(): String = data
}
