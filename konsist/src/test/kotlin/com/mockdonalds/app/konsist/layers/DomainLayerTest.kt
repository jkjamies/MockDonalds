package com.mockdonalds.app.konsist.layers

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates the domain layer conventions:
 * - Abstract use cases live in api modules
 * - Concrete implementations (Impl) live in domain modules
 * - Every abstract use case has a matching Impl
 * - All Impl classes carry @ContributesBinding
 */
class DomainLayerTest : BehaviorSpec({

    Given("use case abstractions") {
        Then("abstract use cases extending CenterPost base types should reside in api modules") {
            Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.hasAbstractModifier && it.hasParent { p ->
                        p.name == "CenterPostInteractor" || p.name == "CenterPostSubjectInteractor"
                    }
                }
                .assertTrue { it.resideInPath("..api..") }
        }
    }

    Given("use case implementations") {
        Then("use case Impl classes should reside in domain modules") {
            Konsist.scopeFromSourceSet("commonMain", "features..", "domain")
                .classes()
                .withNameEndingWith("Impl")
                .filter { !it.name.endsWith("RepositoryImpl") }
                .assertTrue { it.resideInPath("..impl/domain..") }
        }

        Then("every abstract use case should have a matching Impl in domain") {
            val abstractUseCases = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.resideInPath("..api..") &&
                        it.resideInPath("..commonMain..") &&
                        it.hasAbstractModifier && it.hasParent { p ->
                        p.name == "CenterPostInteractor" || p.name == "CenterPostSubjectInteractor"
                    }
                }
                .map { it.name }
                .toSet()

            val domainImpls = Konsist.scopeFromSourceSet("commonMain", "features..", "domain")
                .classes()
                .withNameEndingWith("Impl")
                .map { it.name.removeSuffix("Impl") }
                .toSet()

            abstractUseCases.forEach { useCaseName ->
                assert(useCaseName in domainImpls) {
                    "Missing implementation for abstract use case '$useCaseName' — expected '${useCaseName}Impl' in the feature's domain module"
                }
            }
        }

        Then("use case Impl classes should extend their abstract use case from api") {
            val violators = Konsist.scopeFromSourceSet("commonMain", "features..", "domain")
                .classes()
                .withNameEndingWith("Impl")
                .filter { !it.name.endsWith("RepositoryImpl") }
                .filter { cls ->
                    val expectedParent = cls.name.removeSuffix("Impl")
                    !cls.hasParent { p -> p.name == expectedParent }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Domain use case Impls must extend their matching abstract use case from api:\n$names"
            }
        }

        Then("all use case Impl classes should have @ContributesBinding annotation") {
            Konsist.scopeFromSourceSet("commonMain", "features..", "domain")
                .classes()
                .withNameEndingWith("Impl")
                .filter { !it.name.endsWith("RepositoryImpl") }
                .assertTrue { it.hasAnnotation { a -> a.name == "ContributesBinding" } }
        }
    }
})
