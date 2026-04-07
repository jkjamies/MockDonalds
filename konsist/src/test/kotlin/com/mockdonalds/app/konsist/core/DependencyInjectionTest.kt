package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates that all bindings are properly wired for dependency injection.
 * Every interface/abstract in the domain boundary should have:
 * - A concrete implementation
 * - Proper DI annotation (@ContributesBinding)
 */
class DependencyInjectionTest : BehaviorSpec({

    Given("all interfaces have implementations") {
        Then("every Repository interface should have a @ContributesBinding implementation") {
            val repoInterfaces = Konsist.scopeFromProject()
                .interfaces()
                .withNameEndingWith("Repository")
                .filter { it.resideInPath("..domain..") && it.resideInPath("..commonMain..") }
                .map { it.name }
                .toSet()

            val boundImpls = Konsist.scopeFromProject()
                .classes()
                .withNameEndingWith("RepositoryImpl")
                .filter { it.hasAnnotation { a -> a.name == "ContributesBinding" } }
                .map { it.name.removeSuffix("Impl") }
                .toSet()

            repoInterfaces.forEach { iface ->
                assert(iface in boundImpls) {
                    "Repository interface '$iface' has no @ContributesBinding implementation '${iface}Impl'"
                }
            }
        }

        Then("every abstract use case should have a @ContributesBinding implementation") {
            val abstractUseCases = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.hasAbstractModifier &&
                        it.hasParent { p -> p.name == "CenterPostSubjectInteractor" }
                }
                .map { it.name }
                .toSet()

            val boundImpls = Konsist.scopeFromProject()
                .classes()
                .filter { it.hasAnnotation { a -> a.name == "ContributesBinding" } }
                .map { it.name.removeSuffix("Impl") }
                .toSet()

            abstractUseCases.forEach { useCase ->
                assert(useCase in boundImpls) {
                    "Abstract use case '$useCase' has no @ContributesBinding implementation '${useCase}Impl'"
                }
            }
        }
    }

    Given("all @ContributesBinding classes implement their target") {
        Then("every @ContributesBinding class should extend an interface or abstract class") {
            Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.hasAnnotation { a -> a.name == "ContributesBinding" } &&
                        it.resideInPath("..commonMain..")
                }
                .assertTrue(additionalMessage = "@ContributesBinding classes must implement an interface or extend an abstract class") {
                    it.hasParent { p -> p.name != "Any" }
                }
        }
    }

    Given("presenter DI wiring") {
        Then("all @CircuitInject presenters in commonMain should also have @Inject") {
            Konsist.scopeFromProject()
                .functions()
                .filter {
                    it.hasAnnotation { a -> a.name == "CircuitInject" } &&
                        it.hasAnnotation { a -> a.name == "Composable" } &&
                        it.resideInPath("..commonMain..")
                }
                .assertTrue(additionalMessage = "Circuit presenters must have @Inject for Metro DI") {
                    it.hasAnnotation { a -> a.name == "Inject" }
                }
        }
    }
})
