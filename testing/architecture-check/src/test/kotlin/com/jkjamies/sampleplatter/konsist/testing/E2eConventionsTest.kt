package com.jkjamies.sampleplatter.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces e2e journey conventions in testing/e2e-tests/.
 *
 * E2E tests drive the real, un-minified debug app via UI Automator and must not know anything
 * about the internal structure of the code — no imports from impl modules, no fakes, all cross-screen
 * interaction flows through AppRobot.
 */
class E2eConventionsTest : BehaviorSpec({

    Given("journey test structure") {
        Then("journey tests must be annotated @RunWith(AndroidJUnit4::class)") {
            val journeys = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..testing..e2e-tests..suites..") &&
                        it.nameWithExtension.endsWith("JourneyTest.kt")
                }

            val violators = journeys.filter { file ->
                !file.text.contains("@RunWith(AndroidJUnit4::class)")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Journey tests must be @RunWith(AndroidJUnit4::class):\n$names"
            }
        }

        Then("journey tests must interact through AppRobot") {
            val journeys = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..testing..e2e-tests..suites..") &&
                        it.nameWithExtension.endsWith("JourneyTest.kt")
                }

            val violators = journeys.filter { file -> !file.text.contains("AppRobot") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Journey tests must use AppRobot for all app interactions:\n$names"
            }
        }

        Then("journey tests must not use UiDevice directly") {
            val journeys = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..testing..e2e-tests..suites..") &&
                        it.nameWithExtension.endsWith("JourneyTest.kt")
                }

            val violators = journeys.filter { file ->
                file.imports.any { it.name == "androidx.test.uiautomator.UiDevice" }
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Journey tests must not import UiDevice directly — route through AppRobot:\n$names"
            }
        }
    }

    Given("robot placement") {
        Then("robot files must live under robots/ package") {
            val robotFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..testing..e2e-tests..") &&
                        it.nameWithExtension.endsWith("Robot.kt")
                }

            val violators = robotFiles.filter { !it.path.contains("/robots/") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "E2E robot files must live in testing/e2e-tests/src/main/kotlin/.../robots/:\n$names"
            }
        }
    }

    Given("e2e hygiene") {
        Then("e2e tests must not use println") {
            val e2eFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing..e2e-tests..") }

            val violators = e2eFiles.filter { file -> file.text.contains("println(") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "E2E tests must not use println — rely on assertion failures:\n$names"
            }
        }

        Then("e2e tests must not use Thread.sleep") {
            val e2eFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing..e2e-tests..") }

            val violators = e2eFiles.filter { file -> file.text.contains("Thread.sleep") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "E2E tests must not use Thread.sleep — wait on UiAutomator conditions instead:\n$names"
            }
        }

        Then("e2e tests must not import mock libraries") {
            val e2eFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing..e2e-tests..") }

            val violators = e2eFiles.filter { file ->
                file.imports.any { imp ->
                    imp.name.startsWith("io.mockk.") ||
                        imp.name.startsWith("org.mockito.")
                }
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "E2E tests must not import mockk/Mockito — they drive the real app:\n$names"
            }
        }
    }
})
