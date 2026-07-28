package com.jkjamies.sampleplatter

import android.app.Application
import com.jkjamies.sampleplatter.core.metro.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface ProdAppGraph : AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): ProdAppGraph
    }
}
