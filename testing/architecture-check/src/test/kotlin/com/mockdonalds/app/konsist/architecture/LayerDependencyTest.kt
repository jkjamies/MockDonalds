package com.mockdonalds.app.konsist.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces unidirectional dependency flow between architectural layers.
 *
 * Allowed dependencies: api ← domain ← data, api ← presentation
 * Forbidden: api → domain/data/presentation, domain → data/presentation,
 *            data → presentation, presentation → data
 *
 * Cross-feature imports must only reference another feature's api module.
 */
class LayerDependencyTest : BehaviorSpec({

    Given("api layer isolation") {
        Then("api modules should not import from sibling domain, data, or presentation modules") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..api..") && it.resideInPath("..commonMain..") }
                .flatMap { file ->
                    val featureName = file.path.substringAfter("features/").substringBefore("/")
                    file.imports.filter { import ->
                        val name = import.name
                        (name.contains(".features.$featureName.domain.") ||
                            name.contains(".features.$featureName.data.") ||
                            name.contains(".features.$featureName.presentation.")) &&
                            !name.contains(".features.$featureName.api.")
                    }.map { "${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "API modules must not import from sibling domain/data/presentation layers:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("domain layer isolation") {
        val domainFiles = Konsist.scopeFromProject()
            .files
            .filter { it.resideInPath("..features..") && it.resideInPath("..impl/domain..") && it.resideInPath("..commonMain..") && !it.resideInPath("..api..") }

        Then("domain modules should not import from data packages") {
            domainFiles.assertTrue { file ->
                file.imports.none { import ->
                    import.name.contains(".data.")
                }
            }
        }

        Then("domain modules should not import from presentation packages") {
            domainFiles.assertTrue { file ->
                file.imports.none { import ->
                    import.name.contains(".presentation.")
                }
            }
        }
    }

    Given("data layer isolation") {
        Then("data modules should not import from presentation packages") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..impl/data..") && it.resideInPath("..commonMain..") }
                .assertTrue { file ->
                    file.imports.none { import ->
                        import.name.contains(".presentation.")
                    }
                }
        }
    }

    Given("presentation layer isolation") {
        Then("presentation modules should not import from data packages") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..impl/presentation..") && it.resideInPath("..commonMain..") }
                .assertTrue { file ->
                    file.imports.none { import ->
                        import.name.contains(".data.")
                    }
                }
        }
    }

    Given("network module access restriction") {
        Then("only impl/data modules should import from core:network") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..features..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..impl/data..")
                }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.contains(".core.network.") }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Only impl/data modules may import from core:network — " +
                    "presenters and domain must not depend on HTTP infrastructure:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("cross-feature isolation") {
        Then("feature modules should only import from other features via their api module") {
            val featureFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..commonMain..") }

            val violators = featureFiles.flatMap { file ->
                val featureName = file.path.substringAfter("features/").substringBefore("/")
                file.imports.filter { import ->
                    val name = import.name
                    // Check if importing from a different feature
                    val otherFeatureMatch = Regex("\\.features\\.(\\w+)\\.").find(name)
                    if (otherFeatureMatch != null) {
                        val otherFeature = otherFeatureMatch.groupValues[1]
                        // It's a cross-feature import — only allow .api. packages
                        otherFeature != featureName && !name.contains(".features.$otherFeature.api.")
                    } else {
                        false
                    }
                }.map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Cross-feature imports must only reference another feature's api module:\n${violators.joinToString("\n")}"
            }
        }

        Then("feature modules should not import from core impl packages") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..commonMain..") }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.contains(".core.") && it.name.contains(".impl.") }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Feature modules must only depend on core api packages, never core impl:\n${violators.joinToString("\n")}"
            }
        }

        Then("core modules should not import from feature modules") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..core..") && it.resideInPath("..commonMain..") }
                .flatMap { file ->
                    file.imports
                        .filter { it.name.contains(".features.") }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Core modules must not depend on feature modules:\n${violators.joinToString("\n")}"
            }
        }
    }
})
