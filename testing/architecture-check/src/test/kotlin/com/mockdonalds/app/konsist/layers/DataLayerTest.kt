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
                .assertTrue { it.resideInPath("..impl/domain..") }
        }

        Then("repository interface functions should return Flow, not use suspend") {
            val violators = Konsist.scopeFromProject()
                .interfaces()
                .withNameEndingWith("Repository")
                .filter { it.resideInPath("..impl/domain..") && it.resideInPath("..commonMain..") }
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
                .assertTrue { it.resideInPath("..impl/data..") }
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

    Given("data source conventions") {
        Then("RemoteDataSource classes should reside in a remote package within impl/data") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter {
                    (it.name.contains("RemoteDataSource")) &&
                        it.resideInPath("..commonMain..")
                }
                .filter { !it.resideInPath("..impl/data..") || !it.resideInPath("..remote..") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "RemoteDataSource classes must reside in a remote/ package within impl/data:\n$names"
            }
        }

        Then("RemoteDataSource interfaces should reside in a remote package within impl/data") {
            val violators = Konsist.scopeFromProject()
                .interfaces()
                .filter {
                    (it.name.contains("RemoteDataSource")) &&
                        it.resideInPath("..commonMain..")
                }
                .filter { !it.resideInPath("..impl/data..") || !it.resideInPath("..remote..") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "RemoteDataSource interfaces must reside in a remote/ package within impl/data:\n$names"
            }
        }

        Then("LocalDataSource classes should reside in a local package within impl/data") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter {
                    (it.name.contains("LocalDataSource")) &&
                        it.resideInPath("..commonMain..")
                }
                .filter { !it.resideInPath("..impl/data..") || !it.resideInPath("..local..") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "LocalDataSource classes must reside in a local/ package within impl/data:\n$names"
            }
        }

        Then("LocalDataSource interfaces should reside in a local package within impl/data") {
            val violators = Konsist.scopeFromProject()
                .interfaces()
                .filter {
                    (it.name.contains("LocalDataSource")) &&
                        it.resideInPath("..commonMain..")
                }
                .filter { !it.resideInPath("..impl/data..") || !it.resideInPath("..local..") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "LocalDataSource interfaces must reside in a local/ package within impl/data:\n$names"
            }
        }
    }

    Given("DTO conventions") {
        Then("Dto classes should reside in a remote package within impl/data") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.name.endsWith("Dto") &&
                        it.resideInPath("..commonMain..")
                }
                .filter { !it.resideInPath("..impl/data..") || !it.resideInPath("..remote..") }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Dto classes must reside in a remote/ package within impl/data:\n$names"
            }
        }

        Then("Dto classes should have @Serializable annotation") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.name.endsWith("Dto") &&
                        it.resideInPath("..commonMain..") &&
                        it.resideInPath("..impl/data..")
                }
                .filter { !it.hasAnnotation { a -> a.name == "Serializable" } }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Dto classes must have @Serializable annotation:\n$names"
            }
        }

        Then("Dto classes should be data classes") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.name.endsWith("Dto") &&
                        it.resideInPath("..commonMain..") &&
                        it.resideInPath("..impl/data..")
                }
                .filter { !it.hasDataModifier }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "Dto classes must be data classes:\n$names"
            }
        }
    }
})
