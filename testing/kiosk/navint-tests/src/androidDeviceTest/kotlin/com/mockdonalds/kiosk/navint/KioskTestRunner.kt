package com.mockdonalds.kiosk.navint

import android.app.Application
import android.app.Instrumentation
import androidx.test.runner.AndroidJUnitRunner

class KioskTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: android.content.Context,
    ): Application = Instrumentation.newApplication(KioskTestApplication::class.java, context)
}
