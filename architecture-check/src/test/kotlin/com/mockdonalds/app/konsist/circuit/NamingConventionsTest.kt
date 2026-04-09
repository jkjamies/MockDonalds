package com.mockdonalds.app.konsist.circuit

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates Circuit and architectural naming conventions.
 *
 * Screens end with Screen, events with Event, presenters with Presenter,
 * UI composables with Ui, state with UiState, interactors/repos follow
 * established suffixes.
 */
class NamingConventionsTest : BehaviorSpec({

    Given("Circuit type naming") {
        Then("all Screen objects should end with 'Screen'") {
            Konsist.scopeFromProject()
                .objects()
                .filter { it.hasParent { p -> p.name == "Screen" } }
                .assertTrue { it.name.endsWith("Screen") }
        }

        Then("all sealed event classes should end with 'Event'") {
            Konsist.scopeFromProject()
                .classes()
                .filter { it.hasSealedModifier }
                .filter { it.resideInPath("..impl/presentation..") }
                .filter {
                    // Has subclasses that look like events (data objects or data classes inside)
                    it.name.endsWith("Event")
                }
                .assertTrue { it.name.endsWith("Event") }
        }

        Then("all @CircuitInject functions should end with 'Presenter' or 'Ui'") {
            Konsist.scopeFromProject()
                .functions()
                .filter {
                    it.hasAnnotation { a -> a.name == "CircuitInject" } &&
                        it.hasAnnotation { a -> a.name == "Composable" }
                }
                .assertTrue(additionalMessage = "Circuit-injected functions must end with 'Presenter' or 'Ui'") {
                    it.name.endsWith("Presenter") || it.name.endsWith("Ui")
                }
        }

        Then("all UiState classes should end with 'UiState'") {
            Konsist.scopeFromProject()
                .classes()
                .filter { it.hasDataModifier && it.hasParent { p -> p.name == "CircuitUiState" } }
                .assertTrue { it.name.endsWith("UiState") }
        }
    }

    Given("domain type naming") {
        Then("all use case abstractions should end with a domain noun (not Impl)") {
            Konsist.scopeFromProject()
                .classes()
                .filter { it.hasAbstractModifier && it.hasParent { p -> p.name == "CenterPostSubjectInteractor" } }
                .assertTrue { !it.name.endsWith("Impl") }
        }

        Then("all use case implementations should end with 'Impl'") {
            Konsist.scopeFromProject()
                .classes()
                .filter {
                    !it.hasAbstractModifier &&
                        it.resideInPath("..impl/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        it.hasParent { p -> p.name != "BehaviorSpec" && p.name != "Any" }
                }
                .withNameEndingWith("Impl")
                .assertTrue { it.name.endsWith("Impl") }
        }

        Then("all repository interfaces should end with 'Repository'") {
            Konsist.scopeFromProject()
                .interfaces()
                .filter { it.resideInPath("..impl/domain..") && it.resideInPath("..commonMain..") }
                .assertTrue { it.name.endsWith("Repository") }
        }

        Then("all repository implementations should end with 'RepositoryImpl'") {
            Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..impl/data..") && it.resideInPath("..commonMain..") }
                .withNameEndingWith("Impl")
                .assertTrue { it.name.endsWith("RepositoryImpl") }
        }
    }
})
