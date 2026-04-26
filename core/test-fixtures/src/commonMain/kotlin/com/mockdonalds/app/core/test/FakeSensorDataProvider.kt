package com.mockdonalds.app.core.test

import com.mockdonalds.app.core.network.SensorDataProvider

class FakeSensorDataProvider(
    var data: String = "",
) : SensorDataProvider {
    override suspend fun currentSensorData(): String = data
}
