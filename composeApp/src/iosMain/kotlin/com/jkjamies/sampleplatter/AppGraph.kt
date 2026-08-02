package com.jkjamies.sampleplatter

import com.jkjamies.sampleplatter.core.remoteconfig.HarnessIosBridge
import com.jkjamies.sampleplatter.core.metro.AppGraph
import com.jkjamies.sampleplatter.core.network.AkamaiSensorBridge
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
