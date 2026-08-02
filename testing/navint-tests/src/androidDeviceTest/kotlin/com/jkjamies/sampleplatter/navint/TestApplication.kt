package com.jkjamies.sampleplatter.navint

import android.app.Application
import dev.zacsweers.metro.createGraph

class TestApplication : Application() {
    val graph: NavIntAppGraph by lazy { createGraph<NavIntAppGraph>() }
}
