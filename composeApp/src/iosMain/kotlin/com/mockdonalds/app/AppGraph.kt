package com.mockdonalds.app

import com.mockdonalds.app.core.featureflag.impl.HarnessIosBridge
import com.mockdonalds.app.core.metro.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface ProdAppGraph : AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides harnessIosBridge: HarnessIosBridge): ProdAppGraph
    }
}
