package com.mockdonalds.app.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates test conventions:
 * - Test classes end with Test or Tests suffix
 * - All specs use BehaviorSpec (consistent style)
 * - No runBlocking or runTest (Kotest handles coroutines)
 * - No UnconfinedTestDispatcher (unsafe with concurrent specs)
 */
class TestFileNamingTest : BehaviorSpec({

    val kotestSpecStyles = listOf(
        "BehaviorSpec", "FunSpec", "StringSpec", "DescribeSpec",
        "WordSpec", "FreeSpec", "ShouldSpec", "FeatureSpec",
        "ExpectSpec", "AnnotationSpec",
    )

    Given("test file naming") {
        Then("all test classes should end with Test or Tests suffix") {
            Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") || it.resideInPath("..test/..") }
                .filter { it.hasParent { p -> p.name in kotestSpecStyles } }
                .assertTrue(additionalMessage = "Test classes must end with 'Test' or 'Tests' suffix") {
                    it.name.endsWith("Test") || it.name.endsWith("Tests")
                }
        }
    }

    Given("consistent spec style") {
        Then("all test specs should extend BehaviorSpec") {
            val nonBehaviorSpecs = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") }
                .filter { klass ->
                    klass.hasParent { p -> p.name in kotestSpecStyles && p.name != "BehaviorSpec" }
                }

            assert(nonBehaviorSpecs.isEmpty()) {
                val names = nonBehaviorSpecs.joinToString("\n") { "  ${it.name} (${it.path})" }
                "All test specs must extend BehaviorSpec for consistent Given/When/Then style:\n$names"
            }
        }
    }

    Given("no blocking coroutine patterns in tests") {
        // `core:test-fixtures` is test infrastructure that happens to live in commonMain (it is
        // consumed as a normal dependency by every module's commonTest). Scoping these rules to
        // `..commonTest..` alone left the shared fixtures — the highest-blast-radius test code
        // in the repo — unchecked, which is exactly where the StandardTestDispatcher hang came
        // from.
        val testFiles = Konsist.scopeFromProject()
            .files
            .filter { it.resideInPath("..commonTest..") || it.resideInPath("..core/test-fixtures..") }

        Then("no runBlocking in tests") {
            val violators = testFiles
                .filter { file ->
                    file.imports.any { it.name == "kotlinx.coroutines.runBlocking" }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "runBlocking is not allowed in tests — Kotest handles coroutines natively:\n$names"
            }
        }

        Then("no runTest in tests") {
            val violators = testFiles
                .filter { file ->
                    file.imports.any { it.name == "kotlinx.coroutines.test.runTest" }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "runTest is not needed — Kotest provides coroutine support natively, use TestCenterPostDispatchers:\n$names"
            }
        }

        Then("no UnconfinedTestDispatcher in tests") {
            val violators = testFiles
                .filter { file ->
                    file.imports.any { it.name == "kotlinx.coroutines.test.UnconfinedTestDispatcher" }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "UnconfinedTestDispatcher is not allowed — it is not safe under concurrent spec execution (SpecExecutionMode.LimitedConcurrency). Use TestCenterPostDispatchers:\n$names"
            }
        }

        Then("no StandardTestDispatcher in tests") {
            // StandardTestDispatcher queues onto a TestCoroutineScheduler that only runs when
            // something advances it — and `runTest`, the usual thing that does, is banned above.
            // Code under test doing `withContext(dispatchers.io) { … }` then suspends forever,
            // and the hang cannot be timed out: cancelling it needs that same dead scheduler to
            // resume the continuation, so `withTimeout` and Turbine's own timeout both fail to
            // fire and the Gradle test task hangs until something kills it from outside. This
            // is not hypothetical — it is what `TestCenterPostDispatchers` used to do, and it
            // hung `:features:order:impl:data:testAndroidHostTest` indefinitely in CI.
            val violators = testFiles
                .filter { file ->
                    file.imports.any { it.name == "kotlinx.coroutines.test.StandardTestDispatcher" }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "StandardTestDispatcher is not allowed — nothing in this project advances its " +
                    "scheduler, so dispatched work never runs and the test hangs unkillably. " +
                    "Use TestCenterPostDispatchers:\n$names"
            }
        }
    }
})
