package com.mockdonalds.kiosk

import android.app.Application
import com.mockdonalds.app.core.metro.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface ProdKioskAppGraph : AppGraph {
    val kioskIdleTimer: KioskIdleTimer

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): ProdKioskAppGraph
    }
}
