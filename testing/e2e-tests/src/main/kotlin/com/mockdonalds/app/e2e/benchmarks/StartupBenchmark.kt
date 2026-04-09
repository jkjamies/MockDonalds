package com.mockdonalds.app.e2e.benchmarks

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures app startup time across cold, warm, and hot launch scenarios.
 * Results are captured as Perfetto traces for regression detection.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val targetPackage = "com.mockdonalds.app"

    @Test
    fun coldStartup() {
        benchmarkRule.measureRepeated(
            packageName = targetPackage,
            metrics = listOf(StartupTimingMetric()),
            iterations = 3,
            startupMode = StartupMode.COLD,
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    @Test
    fun warmStartup() {
        benchmarkRule.measureRepeated(
            packageName = targetPackage,
            metrics = listOf(StartupTimingMetric()),
            iterations = 3,
            startupMode = StartupMode.WARM,
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    @Test
    fun hotStartup() {
        benchmarkRule.measureRepeated(
            packageName = targetPackage,
            metrics = listOf(StartupTimingMetric()),
            iterations = 3,
            startupMode = StartupMode.HOT,
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
