package com.mockdonalds.app

import com.mockdonalds.app.core.remoteconfig.HarnessIosBridge
import com.mockdonalds.app.core.metro.AppGraph
import com.mockdonalds.app.core.network.AkamaiSensorBridge
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface ProdAppGraph : AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        // akamaiSensorBridge flows through HttpClientFactory → AkamaiSensorDataProviderIos;
        // Metro prunes it until a feature actually consumes HttpClientFactory, so the
        // binding chain looks unused at the graph boundary. Suppress until the first
        // feature wires up a real HTTP client against the factory.
        @Suppress("UNUSED_GRAPH_INPUT_WARNING")
        fun create(
            @Provides harnessIosBridge: HarnessIosBridge,
            @Provides akamaiSensorBridge: AkamaiSensorBridge,
        ): ProdAppGraph
    }
}
