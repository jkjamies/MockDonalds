package com.mockdonalds.app.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameStartingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates test double conventions:
 * - Fakes and test data live in dedicated test modules, not in commonTest
 * - Every abstract use case has a corresponding Fake
 * - Test doubles are named with Fake or Test prefix
 * - No mockk usage in commonTest (thread-unsafe under concurrent spec execution)
 */
class TestDoubleConventionsTest : BehaviorSpec({

    Given("fake class placement") {
        Then("all Fake classes should reside in a dedicated test module, not commonTest") {
            Konsist.scopeFromProject()
                .classes()
                .withNameStartingWith("Fake")
                .assertTrue(additionalMessage = "Fake classes must live in a dedicated test module (features/*/test/commonMain) or core:test-fixtures, not in a module's commonTest source set") {
                    // Must be in a dedicated test module's commonMain or core:test-fixtures
                    (it.resideInPath("..test/src/commonMain..") || it.resideInPath("..test-fixtures..")) &&
                        !it.resideInPath("..commonTest..")
                }
        }
    }

    Given("no fakes or test data in module commonTest") {
        Then("commonTest source sets should not contain Fake classes") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") }
                .withNameStartingWith("Fake")

            assert(violators.isEmpty()) {
                val names = violators.joinToString { "${it.name} (${it.path})" }
                "Fake classes found in commonTest — move them to a dedicated test module: $names"
            }
        }

        Then("commonTest source sets should not contain test data objects or companion DEFAULT values") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") }
                .filter {
                    it.name.startsWith("Fake") ||
                        it.name.startsWith("Stub") ||
                        (it.name.startsWith("Mock") && !it.name.startsWith("MockDonalds")) ||
                        it.name.endsWith("Mock") ||
                        it.name.startsWith("TestData") ||
                        it.name.endsWith("TestData") ||
                        it.name.endsWith("Fixture") ||
                        it.name.endsWith("Factory")
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString { "${it.name} (${it.path})" }
                "Test data classes found in commonTest — move them to a dedicated test module: $names"
            }
        }
    }

    Given("fake coverage") {
        Then("every abstract use case in api should have a Fake in a test module") {
            val abstractUseCases = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.hasAbstractModifier &&
                        it.hasParent { p -> p.name == "CenterPostSubjectInteractor" } &&
                        it.resideInPath("..api..") &&
                        it.resideInPath("..commonMain..")
                }
                .map { it.name }
                .toSet()

            val fakes = Konsist.scopeFromProject()
                .classes()
                .withNameStartingWith("Fake")
                .map { it.name.removePrefix("Fake") }
                .toSet()

            abstractUseCases.forEach { useCaseName ->
                assert(useCaseName in fakes) {
                    "Missing Fake for use case '$useCaseName' — expected 'Fake$useCaseName' in the feature's test module"
                }
            }
        }
    }

    Given("test double naming") {
        Then("test doubles extending production types should be prefixed with Fake or Test") {
            val testModuleClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..test..") || it.resideInPath("..test-fixtures..") }
                .filter {
                    it.hasParent { p ->
                        p.name == "CenterPostSubjectInteractor" ||
                            p.name.endsWith("Repository") ||
                            p.name.endsWith("Dispatchers")
                    }
                }

            testModuleClasses.assertTrue(additionalMessage = "Test doubles extending production interfaces/classes must be prefixed with 'Fake' or 'Test'") {
                it.name.startsWith("Fake") || it.name.startsWith("Test")
            }
        }
    }

    Given("no mockk usage") {
        Then("commonTest should not import mockk — use fakes instead for concurrent spec safety") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..commonTest..") }
                .filter { file ->
                    file.imports.any { it.name.startsWith("io.mockk") }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "mockk imports found in commonTest — mockk is not thread-safe under concurrent spec execution " +
                    "(SpecExecutionMode.LimitedConcurrency). Use fakes in dedicated test modules instead:\n$names"
            }
        }
    }
})
