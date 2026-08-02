package com.mockdonalds.app.konsist.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import com.mockdonalds.app.konsist.featurePackageSegment
import com.mockdonalds.app.konsist.isCoreImplPath
import com.mockdonalds.app.konsist.isProductionSourcePath
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces unidirectional dependency flow between architectural layers.
 *
 * Allowed dependencies: api ← domain ← data, api ← presentation
 * Forbidden: api → domain/data/presentation, domain → data/presentation,
 *            data → presentation, presentation → data
 *
 * Cross-feature imports must only reference another feature's api module.
 *
 * Scope: all production source sets (`commonMain`, `androidMain`, `iosMain`) — see
 * `isProductionSourcePath`.
 */
class LayerDependencyTest : BehaviorSpec({

    val productionFiles = Konsist.scopeFromProject()
        .files
        .filter { isProductionSourcePath(it.path) }

    Given("api layer isolation") {
        Then("api modules should not import from sibling domain, data, or presentation modules") {
            val violators = productionFiles
                .filter { it.resideInPath("..features..") && it.resideInPath("..api..") }
                .flatMap { file ->
                    val pkg = featurePackageSegment(file.path)
                    file.imports.filter { import ->
                        val name = import.name
                        (name.contains(".features.$pkg.domain.") ||
                            name.contains(".features.$pkg.data.") ||
                            name.contains(".features.$pkg.presentation.")) &&
                            !name.contains(".features.$pkg.api.")
                    }.map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "API modules must not import from sibling domain/data/presentation layers:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("domain layer isolation") {
        val domainFiles = productionFiles.filter {
            it.resideInPath("..features..") &&
                it.resideInPath("..impl/domain..") &&
                !it.resideInPath("..api..")
        }

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
            productionFiles
                .filter { it.resideInPath("..features..") && it.resideInPath("..impl/data..") }
                .assertTrue { file ->
                    file.imports.none { import ->
                        import.name.contains(".presentation.")
                    }
                }
        }
    }

    Given("presentation layer isolation") {
        Then("presentation modules should not import from data packages") {
            productionFiles
                .filter { it.resideInPath("..features..") && it.resideInPath("..impl/presentation..") }
                .assertTrue { file ->
                    file.imports.none { import ->
                        import.name.contains(".data.")
                    }
                }
        }
    }

    Given("network module access restriction") {
        Then("only impl/data modules should import from core:network") {
            val violators = productionFiles
                .filter { it.resideInPath("..features..") && !it.resideInPath("..impl/data..") }
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
            val featureFiles = productionFiles.filter { it.resideInPath("..features..") }

            val violators = featureFiles.flatMap { file ->
                val ownPackageSegment = featurePackageSegment(file.path)
                file.imports.filter { import ->
                    val name = import.name
                    val otherFeatureMatch = Regex("\\.features\\.(\\w+)\\.").find(name)
                    if (otherFeatureMatch != null) {
                        val otherFeature = otherFeatureMatch.groupValues[1]
                        // Cross-feature import — only another feature's .api. packages are allowed
                        otherFeature != ownPackageSegment && !name.contains(".features.$otherFeature.api.")
                    } else {
                        false
                    }
                }.map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Cross-feature imports must only reference another feature's api module:\n${violators.joinToString("\n")}"
            }
        }

        Then("feature modules should not import from core impl modules") {
            // The impl surface is derived from the module PATH, not from the package name.
            // `core:analytics:impl`, `core:logger:impl` and `core:remote-config:impl` namespace
            // their impl types under an `.impl` package, but `core:auth`, `core:build-config`,
            // `core:network` and `core:persistence` share their api module's package. Matching
            // on a ".impl." substring therefore passes silently on four of the seven split
            // modules — a feature could import `InMemoryAuthManager` or `HttpClientFactoryImpl`
            // directly and nothing would flag it.
            val coreImplTypeNames = (
                Konsist.scopeFromProject().classes().filter { isCoreImplPath(it.path) } +
                    Konsist.scopeFromProject().interfaces().filter { isCoreImplPath(it.path) }
                )
                .map { it.name }
                .toSet()

            val violators = productionFiles
                .filter { it.resideInPath("..features..") }
                .flatMap { file ->
                    file.imports
                        .filter {
                            it.name.contains(".core.") &&
                                it.name.substringAfterLast('.') in coreImplTypeNames
                        }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Feature modules must only depend on core api packages, never core impl:\n${violators.joinToString("\n")}"
            }
        }

        Then("core modules should not import from feature modules") {
            val violators = productionFiles
                .filter { it.resideInPath("..core..") }
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
