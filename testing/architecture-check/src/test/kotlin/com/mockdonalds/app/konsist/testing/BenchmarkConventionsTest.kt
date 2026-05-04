package com.mockdonalds.app.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces macrobenchmark conventions in testing/mobile/benchmarks/.
 *
 * Benchmarks target the minified `benchmark` build type of :androidApp and run out-of-process
 * via self-instrumenting. They must not pull in Compose UI test deps (they crash the target on
 * launch because R8 has stripped symbols those deps reflect on), and they cannot reach into
 * impl/domain or impl/data because R8 renames those classes anyway.
 */
class BenchmarkConventionsTest : BehaviorSpec({

    Given("benchmark file structure") {
        Then("benchmark classes must be annotated @RunWith(AndroidJUnit4::class)") {
            val benchmarks = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..testing..benchmarks..") &&
                        it.nameWithExtension.endsWith("Benchmark.kt")
                }

            val violators = benchmarks.filter { file ->
                !file.text.contains("@RunWith(AndroidJUnit4::class)")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Benchmark classes must be @RunWith(AndroidJUnit4::class):\n$names"
            }
        }

        Then("benchmark classes must declare a MacrobenchmarkRule @get:Rule") {
            val benchmarks = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..testing..benchmarks..") &&
                        it.nameWithExtension.endsWith("Benchmark.kt")
                }

            val violators = benchmarks.filter { file ->
                !file.text.contains("MacrobenchmarkRule()")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Benchmark classes must expose a MacrobenchmarkRule via @get:Rule:\n$names"
            }
        }
    }

    Given("benchmark dependency boundaries") {
        Then("benchmarks must not import compose.ui.test") {
            val benchmarks = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing..benchmarks..") }

            val violators = benchmarks.filter { file ->
                file.imports.any { it.name.contains("androidx.compose.ui.test") }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val bad = file.imports.filter { it.name.contains("androidx.compose.ui.test") }
                    "  ${file.name}: ${bad.joinToString { it.name }}"
                }
                "Benchmarks must not import androidx.compose.ui.test — it crashes R8-minified targets:\n$details"
            }
        }

        Then("benchmarks must not import from feature test modules") {
            val benchmarks = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing..benchmarks..") }

            val violators = benchmarks.filter { file ->
                file.imports.any { imp ->
                    imp.name.contains(".test.") &&
                        imp.name.contains(".features.")
                }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val bad = file.imports.filter { imp ->
                        imp.name.contains(".test.") && imp.name.contains(".features.")
                    }
                    "  ${file.name}: ${bad.joinToString { it.name }}"
                }
                "Benchmarks must not import feature test/ modules — benchmarks measure the real minified app:\n$details"
            }
        }

        Then("benchmarks must not import from impl/domain or impl/data") {
            val benchmarks = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing..benchmarks..") }

            val violators = benchmarks.filter { file ->
                file.imports.any { imp ->
                    imp.name.contains(".impl.domain.") || imp.name.contains(".impl.data.")
                }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val bad = file.imports.filter { imp ->
                        imp.name.contains(".impl.domain.") || imp.name.contains(".impl.data.")
                    }
                    "  ${file.name}: ${bad.joinToString { it.name }}"
                }
                "Benchmarks must not import impl/domain or impl/data — R8 renames those classes; use UiAutomator:\n$details"
            }
        }
    }

    Given("benchmark hygiene") {
        Then("benchmarks must not use println") {
            val benchmarks = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing..benchmarks..") }

            val violators = benchmarks.filter { file -> file.text.contains("println(") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Benchmarks must not use println — emit structured metrics via MacrobenchmarkRule:\n$names"
            }
        }

        Then("benchmarks must not use Thread.sleep") {
            val benchmarks = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing..benchmarks..") }

            val violators = benchmarks.filter { file -> file.text.contains("Thread.sleep") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Benchmarks must not use Thread.sleep — wait on UiAutomator conditions instead:\n$names"
            }
        }
    }
})
