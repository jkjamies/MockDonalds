package com.mockdonalds.kiosk.navint

import android.app.Application
import dev.zacsweers.metro.createGraph

class KioskTestApplication : Application() {
    val graph: KioskNavIntAppGraph by lazy { createGraph<KioskNavIntAppGraph>() }
}
