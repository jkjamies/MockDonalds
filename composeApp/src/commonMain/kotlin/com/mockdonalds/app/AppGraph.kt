package com.mockdonalds.app

import com.mockdonalds.app.core.metro.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(AppScope::class)
interface ProdAppGraph : AppGraph
