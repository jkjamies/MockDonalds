package com.mockdonalds.kiosk.navint

import com.mockdonalds.app.core.metro.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

/**
 * Test-graph for kiosk navint suite. Wires `core:*` real bindings + kiosk
 * feature impl/presentation (real presenters) + features/order test fakes.
 * No kiosk app shell — tests compose presenters directly via [setKioskNavIntContent].
 */
@DependencyGraph(AppScope::class)
interface KioskNavIntAppGraph : AppGraph
