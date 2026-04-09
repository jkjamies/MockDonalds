package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates that types default to minimal visibility.
 * Classes, interfaces, and functions should not be public unless necessary.
 */
class VisibilityConventionsTest : BehaviorSpec({

    Given("domain layer visibility") {
        Then("@ContributesBinding classes should be public for Metro DI cross-module resolution") {
            Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.hasAnnotation { a -> a.name == "ContributesBinding" } &&
                        it.resideInPath("..commonMain..")
                }
                .assertTrue(additionalMessage = "@ContributesBinding classes must be public — Metro resolves them across module boundaries") {
                    it.hasPublicOrDefaultModifier
                }
        }
    }

    Given("domain module visibility") {
        Then("only Repository interfaces and DI-bound Impls should be public in domain modules") {
            val domainDeclarations = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.resideInPath("..impl/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..api..") &&
                        it.hasPublicOrDefaultModifier &&
                        !it.hasInternalModifier
                }

            val unexpectedPublic = domainDeclarations
                .filter {
                    !it.name.endsWith("Impl") &&
                        !it.name.endsWith("Repository")
                }

            assert(unexpectedPublic.isEmpty()) {
                val names = unexpectedPublic.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Only Repository interfaces and Impl classes should be public in domain modules:\n$names"
            }
        }

        Then("top-level functions in domain modules should not be public") {
            val publicFunctions = Konsist.scopeFromProject()
                .functions()
                .filter {
                    it.resideInPath("..impl/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..api..") &&
                        !it.hasPrivateModifier &&
                        !it.hasInternalModifier &&
                        !it.hasProtectedModifier &&
                        // Exclude override functions and interface members
                        !it.hasOverrideModifier &&
                        it.isTopLevel
                }

            assert(publicFunctions.isEmpty()) {
                val names = publicFunctions.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Non-override functions in domain modules should be internal or private:\n$names"
            }
        }
    }

    Given("presentation layer visibility") {
        Then("UiState classes should not be internal (they cross module boundaries via Circuit)") {
            Konsist.scopeFromProject()
                .classes()
                .filter { it.name.endsWith("UiState") && it.hasDataModifier }
                .assertTrue { !it.hasInternalModifier }
        }
    }
})
