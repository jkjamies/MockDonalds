package com.mockdonalds.app.konsist.layers

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates the presentation layer conventions:
 * - Presenters are annotated with @CircuitInject and @Inject
 * - UiState data classes implement CircuitUiState and have eventSink
 * - UiState and Event types reside in presentation modules
 */
class PresentationLayerTest : BehaviorSpec({

    Given("Circuit presenters") {
        Then("all presenter functions should have @CircuitInject annotation") {
            Konsist.scopeFromSourceSet("commonMain", "features..", "presentation")
                .functions()
                .withNameEndingWith("Presenter")
                .filter { it.hasAnnotation { a -> a.name == "Composable" } }
                .assertTrue { it.hasAnnotation { a -> a.name == "CircuitInject" } }
        }
    }

    Given("presenter responsibilities") {
        Then("presenter files should only contain one public function (the presenter itself)") {
            val presenterFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..presentation..") &&
                        it.resideInPath("..commonMain..") &&
                        it.name.endsWith("Presenter.kt")
                }

            val violators = presenterFiles.mapNotNull { file ->
                val publicFunctions = file.functions()
                    .filter { !it.hasPrivateModifier && !it.hasInternalModifier }
                if (publicFunctions.size > 1) {
                    val extras = publicFunctions.filter { !it.name.endsWith("Presenter") }
                        .joinToString { it.name }
                    "  ${file.name}: extra public functions: $extras"
                } else null
            }

            assert(violators.isEmpty()) {
                "Presenter files should only expose the presenter function — " +
                    "extract additional logic to use cases or private helpers:\n${violators.joinToString("\n")}"
            }
        }

        Then("presenters should not depend on repositories") {
            val presenterFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..presentation..") &&
                        it.resideInPath("..commonMain..") &&
                        it.name.endsWith("Presenter.kt")
                }

            val violators = presenterFiles
                .flatMap { file ->
                    file.imports
                        .filter { it.name.contains(".domain.") && it.name.endsWith("Repository") }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Presenters must not depend on repositories directly — use use cases (interactors) instead:\n${violators.joinToString("\n")}"
            }
        }

        Then("presenters should not construct api domain models directly") {
            // Get all data class names from api domain packages
            val apiModelNames = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.hasDataModifier &&
                        it.resideInPath("..api..") &&
                        it.resideInPath("..domain..") &&
                        it.resideInPath("..commonMain..")
                }
                .map { it.name }
                .toSet()

            // Check presenter files for direct construction of api models
            val presenterFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..presentation..") &&
                        it.resideInPath("..commonMain..") &&
                        it.name.endsWith("Presenter.kt")
                }

            val violators = presenterFiles.flatMap { file ->
                apiModelNames.filter { modelName ->
                    // Look for direct constructor calls like "ModelName("
                    file.text.contains("$modelName(")
                }.map { "  ${file.name} constructs $it" }
            }

            assert(violators.isEmpty()) {
                "Presenters should not construct api domain models directly — " +
                    "that is domain logic and belongs in use cases:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("UiState conventions") {
        Then("all UiState data classes should implement CircuitUiState") {
            Konsist.scopeFromProject()
                .classes()
                .withNameEndingWith("UiState")
                .filter { it.hasDataModifier }
                .assertTrue { it.hasParent { p -> p.name == "CircuitUiState" } }
        }

        Then("all UiState data classes should have an eventSink property") {
            Konsist.scopeFromProject()
                .classes()
                .withNameEndingWith("UiState")
                .filter { it.hasDataModifier }
                .assertTrue { klass ->
                    klass.hasProperty { it.name == "eventSink" }
                }
        }

        Then("all UiState classes should reside in presentation modules") {
            Konsist.scopeFromProject()
                .classes()
                .withNameEndingWith("UiState")
                .assertTrue { it.resideInPath("..presentation..") }
        }
    }
})
