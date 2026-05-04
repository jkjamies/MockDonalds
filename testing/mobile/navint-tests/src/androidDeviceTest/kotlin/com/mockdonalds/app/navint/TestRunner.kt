package com.mockdonalds.app.navint

import android.app.Application
import android.app.Instrumentation
import androidx.test.runner.AndroidJUnitRunner

class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: android.content.Context,
    ): Application = Instrumentation.newApplication(TestApplication::class.java, context)
}
