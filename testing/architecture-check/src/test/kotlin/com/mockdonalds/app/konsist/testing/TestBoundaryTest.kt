package com.mockdonalds.app.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces clean separation between test levels:
 * - Feature UI tests (androidDeviceTest) test individual component rendering via Robot pattern
 * - Navigation/integration tests (navint-tests) test cross-feature navigation and state
 * - E2E tests (e2e-tests) test full user journeys against the real app — no fakes
 *
 * Each level should stay in its lane — no mixing of test patterns.
 */
class TestBoundaryTest : BehaviorSpec({

    Given("feature UI test boundaries") {
        Then("feature UI tests should not contain navigation test patterns") {
            val featureUiTests = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..androidDeviceTest..") &&
                        it.nameWithExtension.endsWith("Test.kt")
                }

            val violators = featureUiTests.filter { file ->
                file.text.contains("Navigator") &&
                    !file.text.contains("FakeNavigator") // FakeNavigator is OK in presenter tests
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Feature UI tests must not use real Navigator — use navint-tests for navigation testing:\n$names"
            }
        }

        Then("feature UI tests should not navigate between screens") {
            val featureUiTests = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..androidDeviceTest..") &&
                        it.nameWithExtension.endsWith("Test.kt")
                }

            val violators = featureUiTests.filter { file ->
                file.text.contains("resetRoot(") || file.text.contains("navigator.goTo(")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Feature UI tests must not call resetRoot() or navigator.goTo() — use navint-tests for navigation:\n$names"
            }
        }
    }

    Given("e2e-tests boundaries") {
        Then("e2e-tests should not import from feature test modules") {
            val e2eFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..e2e-tests..") }

            val violators = e2eFiles.filter { file ->
                file.imports.any { imp ->
                    imp.name.contains(".test.") &&
                        imp.name.contains(".features.")
                }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val badImports = file.imports.filter { imp ->
                        imp.name.contains(".test.") && imp.name.contains(".features.")
                    }
                    "  ${file.name}: ${badImports.joinToString { it.name }}"
                }
                "e2e-tests must not import from feature test/ modules — e2e tests are fully real:\n$details"
            }
        }

        Then("e2e-tests should not import from impl/domain or impl/data") {
            val e2eFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..e2e-tests..") }

            val violators = e2eFiles.filter { file ->
                file.imports.any { imp ->
                    (imp.name.contains(".impl.domain.") || imp.name.contains(".impl.data.")) &&
                        !imp.name.contains(".impl.presentation.")
                }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val badImports = file.imports.filter { imp ->
                        imp.name.contains(".impl.domain.") || imp.name.contains(".impl.data.")
                    }
                    "  ${file.name}: ${badImports.joinToString { it.name }}"
                }
                "e2e-tests must not import from impl/domain or impl/data — e2e tests interact via UI only:\n$details"
            }
        }

        Then("e2e journey tests should end with JourneyTest") {
            val e2eSuites = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..e2e-tests..suites..") &&
                        it.nameWithExtension.endsWith("Test.kt")
                }

            val violators = e2eSuites.filter { !it.name.endsWith("JourneyTest") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "E2E journey test files in suites/ must end with JourneyTest:\n$names"
            }
        }

        Then("e2e benchmark files should end with Benchmark") {
            val e2eBenchmarks = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..e2e-tests..benchmarks..") &&
                        it.nameWithExtension.endsWith(".kt")
                }

            val violators = e2eBenchmarks.filter { !it.name.endsWith("Benchmark") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "E2E benchmark files in benchmarks/ must end with Benchmark:\n$names"
            }
        }
    }

    Given("navint-tests boundaries") {
        Then("navint-tests should not import feature-level Robot classes") {
            val navintTests = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..navint-tests..") &&
                        it.nameWithExtension.endsWith("Test.kt")
                }

            val violators = navintTests.filter { file ->
                file.imports.any { imp ->
                    imp.name.contains(".presentation.") &&
                        (imp.name.contains("UiRobot") || imp.name.contains("StateRobot"))
                }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val badImports = file.imports.filter { imp ->
                        imp.name.contains(".presentation.") &&
                            (imp.name.contains("UiRobot") || imp.name.contains("StateRobot"))
                    }
                    "  ${file.name}: ${badImports.joinToString { it.name }}"
                }
                "navint-tests must not import feature-level UiRobot/StateRobot — define navint-specific robots instead:\n$details"
            }
        }

        Then("navint-tests should not import from impl/domain or impl/data") {
            val navintFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..navint-tests..") }

            val violators = navintFiles.filter { file ->
                file.imports.any { imp ->
                    (imp.name.contains(".impl.domain.") || imp.name.contains(".impl.data.")) &&
                        !imp.name.contains(".impl.presentation.")
                }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val badImports = file.imports.filter { imp ->
                        imp.name.contains(".impl.domain.") || imp.name.contains(".impl.data.")
                    }
                    "  ${file.name}: ${badImports.joinToString { it.name }}"
                }
                "navint-tests must not import from impl/domain or impl/data — fakes are sole bindings:\n$details"
            }
        }
    }
})
