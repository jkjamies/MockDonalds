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
        val testFiles = Konsist.scopeFromProject()
            .files
            .filter { it.resideInPath("..commonTest..") }

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
                "UnconfinedTestDispatcher is not allowed — it is not safe under concurrent spec execution (SpecExecutionMode.LimitedConcurrency). Use StandardTestDispatcher via TestCenterPostDispatchers:\n$names"
            }
        }
    }
})
