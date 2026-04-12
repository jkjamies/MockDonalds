package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces feature flag consumption patterns:
 * - Presenters must use ObserveFeatureFlag interactor, not FeatureFlagProvider directly
 * - Domain and data layers must use FeatureFlagProvider, not the ObserveFeatureFlag interactor
 */
class FeatureFlagConventionsTest : BehaviorSpec({

    Given("feature flag consumption in presentation layer") {
        Then("presenters should not import FeatureFlagProvider directly") {
            val presenterFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/presentation..") &&
                        it.resideInPath("..commonMain..")
                }

            val violators = presenterFiles.flatMap { file ->
                file.imports
                    .filter { it.name == "com.mockdonalds.app.core.featureflag.FeatureFlagProvider" }
                    .map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Presenters must use ObserveFeatureFlag interactor, not FeatureFlagProvider directly:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("feature flag consumption in domain layer") {
        Then("domain classes should not import ObserveFeatureFlag") {
            val domainFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..api..")
                }

            val violators = domainFiles.flatMap { file ->
                file.imports
                    .filter { it.name == "com.mockdonalds.app.core.featureflag.ObserveFeatureFlag" }
                    .map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Domain layer must use FeatureFlagProvider, not the ObserveFeatureFlag interactor:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("feature flag consumption in data layer") {
        Then("data classes should not import ObserveFeatureFlag") {
            val dataFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/data..") &&
                        it.resideInPath("..commonMain..")
                }

            val violators = dataFiles.flatMap { file ->
                file.imports
                    .filter { it.name == "com.mockdonalds.app.core.featureflag.ObserveFeatureFlag" }
                    .map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Data layer must use FeatureFlagProvider, not the ObserveFeatureFlag interactor:\n${violators.joinToString("\n")}"
            }
        }
    }
})
