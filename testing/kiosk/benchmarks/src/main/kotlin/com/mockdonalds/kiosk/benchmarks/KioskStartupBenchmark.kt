package com.mockdonalds.kiosk.benchmarks

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures kiosk app startup time for cold and warm launch scenarios against
 * the kiosk's benchmark build variant (R8-minified, profileable for Perfetto).
 * Cold startup loads `MockDonaldsKioskApp` and renders `AttractScreen`.
 *
 * Hot startup is intentionally omitted: on emulator + R8 it completes too fast
 * for `StartupTimingMetric` to observe a trace event ("Unable to read any
 * metrics" failures). Mirrors the consumer benchmarks decision.
 */
@RunWith(AndroidJUnit4::class)
class KioskStartupBenchmark {

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
        // return the test APK's package. Derived from kiosk applicationId
        // "com.mockdonalds.kiosk" + market flavor "core" suffix ".core".
        const val TARGET_PACKAGE = "com.mockdonalds.kiosk.core"
    }
}
