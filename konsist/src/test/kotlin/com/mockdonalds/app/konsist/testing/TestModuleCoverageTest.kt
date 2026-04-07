package com.mockdonalds.app.konsist.testing

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import io.kotest.core.spec.style.BehaviorSpec
import java.io.File

/**
 * Validates test coverage requirements:
 * - Every feature has a dedicated test module
 * - Every domain Impl has a test file
 * - Every presenter has a test file
 * - Every repository impl has a test file
 */
class TestModuleCoverageTest : BehaviorSpec({

    Given("feature test module coverage") {
        Then("every feature should have a dedicated test module") {
            val projectRoot = Konsist.scopeFromProject()
                .files
                .first()
                .path
                .substringBefore("/features/")
                .let { if (it.contains("/core/")) it.substringBefore("/core/") else it }

            val featuresDir = File("$projectRoot/features")
            val features = featuresDir.listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?: emptyList()

            val missingTestModules = features.filter { feature ->
                !File("$projectRoot/features/$feature/test").exists()
            }

            assert(missingTestModules.isEmpty()) {
                "Features missing dedicated test modules (features/<name>/test/):\n${missingTestModules.joinToString("\n") { "  :features:$it:test" }}"
            }
        }
    }

    Given("domain implementation test coverage") {
        Then("every use case Impl should have a corresponding test file") {
            val implClasses = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.resideInPath("..domain..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..api..")
                }
                .withNameEndingWith("Impl")
                .filter { !it.name.endsWith("RepositoryImpl") }
                .map { it.name }
                .toSet()

            val testClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") }
                .withNameEndingWith("Test")
                .map { it.name.removeSuffix("Test") }
                .toSet()

            val untested = implClasses.filter { it !in testClasses }

            assert(untested.isEmpty()) {
                "Use case implementations missing test files:\n${untested.joinToString("\n") { "  $it — expected ${it}Test" }}"
            }
        }
    }

    Given("presenter test coverage") {
        Then("every presenter should have a corresponding test file") {
            val presenters = Konsist.scopeFromProject()
                .functions()
                .filter {
                    it.hasAnnotation { a -> a.name == "CircuitInject" } &&
                        it.hasAnnotation { a -> a.name == "Composable" } &&
                        it.resideInPath("..commonMain..")
                }
                .filter { it.name.endsWith("Presenter") }
                .map { it.name }
                .toSet()

            val testClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") }
                .withNameEndingWith("Test")
                .map { it.name.removeSuffix("Test") }
                .toSet()

            val untested = presenters.filter { it !in testClasses }

            assert(untested.isEmpty()) {
                "Presenters missing test files:\n${untested.joinToString("\n") { "  $it — expected ${it}Test" }}"
            }
        }
    }

    Given("repository test coverage") {
        Then("every repository implementation should have a corresponding test file") {
            val repoImpls = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.resideInPath("..data..") &&
                        it.resideInPath("..commonMain..")
                }
                .withNameEndingWith("RepositoryImpl")
                .map { it.name }
                .toSet()

            val testClasses = Konsist.scopeFromProject()
                .classes()
                .filter { it.resideInPath("..commonTest..") }
                .withNameEndingWith("Test")
                .map { it.name.removeSuffix("Test") }
                .toSet()

            val untested = repoImpls.filter { it !in testClasses }

            assert(untested.isEmpty()) {
                "Repository implementations missing test files:\n${untested.joinToString("\n") { "  $it — expected ${it}Test" }}"
            }
        }
    }
})
