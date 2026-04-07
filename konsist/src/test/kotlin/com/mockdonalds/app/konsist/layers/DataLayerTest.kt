package com.mockdonalds.app.konsist.layers

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates the data layer conventions:
 * - Repository interfaces live in domain modules
 * - RepositoryImpl classes live in data modules
 * - Every interface has a matching Impl
 * - Impl classes implement their interface
 * - All carry @ContributesBinding
 */
class DataLayerTest : BehaviorSpec({

    Given("repository interfaces") {
        Then("every Repository interface should reside in a domain module") {
            Konsist.scopeFromProject()
                .interfaces()
                .withNameEndingWith("Repository")
                .assertTrue { it.resideInPath("..domain..") }
        }

        Then("repository interface functions should return Flow, not use suspend") {
            val violators = Konsist.scopeFromProject()
                .interfaces()
                .withNameEndingWith("Repository")
                .filter { it.resideInPath("..domain..") && it.resideInPath("..commonMain..") }
                .flatMap { iface ->
                    iface.functions().filter { fn ->
                        fn.text.trimStart().startsWith("suspend ")
                    }.map { "  ${iface.name}.${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Repository interface functions must return Flow, not use suspend — " +
                    "suspend functions break the reactive stream pattern:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("repository implementations") {
        Then("every RepositoryImpl class should reside in a data module") {
            Konsist.scopeFromProject()
                .classes()
                .withNameEndingWith("RepositoryImpl")
                .assertTrue { it.resideInPath("..data..") }
        }

        Then("every Repository interface should have a corresponding RepositoryImpl") {
            val repoImpls = Konsist.scopeFromSourceSet("commonMain", "features..", "data")
                .classes()
                .withNameEndingWith("RepositoryImpl")
                .map { it.name.removeSuffix("Impl") }
                .toSet()

            Konsist.scopeFromSourceSet("commonMain", "features..", "domain")
                .interfaces()
                .withNameEndingWith("Repository")
                .assertTrue { it.name in repoImpls }
        }

        Then("every RepositoryImpl should implement its corresponding Repository interface") {
            Konsist.scopeFromSourceSet("commonMain", "features..", "data")
                .classes()
                .withNameEndingWith("RepositoryImpl")
                .assertTrue { klass ->
                    val expectedInterface = klass.name.removeSuffix("Impl")
                    klass.hasParent { it.name == expectedInterface }
                }
        }

        Then("all RepositoryImpl classes should have @ContributesBinding annotation") {
            Konsist.scopeFromSourceSet("commonMain", "features..", "data")
                .classes()
                .withNameEndingWith("RepositoryImpl")
                .assertTrue { it.hasAnnotation { a -> a.name == "ContributesBinding" } }
        }
    }
})
