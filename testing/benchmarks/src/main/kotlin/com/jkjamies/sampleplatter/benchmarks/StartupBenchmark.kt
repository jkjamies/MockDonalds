package com.jkjamies.sampleplatter.benchmarks

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures app startup time for cold and warm launch scenarios against the
 * target app's benchmark variant (R8-minified, profileable for Perfetto).
 * Results are captured as Perfetto traces for regression detection.
 *
 * Hot startup is intentionally omitted: on emulator + R8 targets it completes
 * too fast for `StartupTimingMetric` to observe a trace event, causing
 * "Unable to read any metrics" failures. The metric is also of limited value
 * because hot startup for a well-behaved Compose app is effectively instant.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
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
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            iterations = 3,
            startupMode = StartupMode.WARM,
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    private companion object {
        // Hardcoded because self-instrumenting makes `targetContext.packageName`
        // return the test APK's package. Derived from applicationId
        // "com.jkjamies.sampleplatter" + market flavor "core" suffix ".core".
        const val TARGET_PACKAGE = "com.jkjamies.sampleplatter.core"
    }
}
