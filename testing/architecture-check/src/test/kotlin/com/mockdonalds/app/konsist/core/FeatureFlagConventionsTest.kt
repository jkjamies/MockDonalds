package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces feature flag consumption patterns:
 * - Presenters must read flags via `FeatureFlagProvider.rememberFlag(flag)` — never
 *   `.isEnabled(...)` or `.observe(...)` directly (those bypass Compose state and
 *   won't react to flag changes).
 * - Domain and data layers must use `FeatureFlagProvider.isEnabled(...)` or
 *   `.observe(...)` — never `rememberFlag` (it's Composable and presenter-only).
 */
class FeatureFlagConventionsTest : BehaviorSpec({

    Given("feature flag consumption in presentation layer") {
        Then("presenters must not call FeatureFlagProvider.isEnabled or .observe directly") {
            val presenterFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/presentation..") &&
                        it.resideInPath("..commonMain..") &&
                        it.imports.any { import ->
                            import.name == "com.mockdonalds.app.core.featureflag.FeatureFlagProvider"
                        }
                }

            val violators = presenterFiles.filter { file ->
                file.text.contains(".isEnabled(") || file.text.contains(".observe(")
            }.map { "  ${it.name}" }

            assert(violators.isEmpty()) {
                "Presenters must read flags via FeatureFlagProvider.rememberFlag(flag), " +
                    "not .isEnabled(...) or .observe(...) directly:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("feature flag consumption in domain layer") {
        Then("domain classes must not reference rememberFlag") {
            val domainFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..api..")
                }

            val violators = domainFiles.filter { it.text.contains("rememberFlag") }
                .map { "  ${it.name}" }

            assert(violators.isEmpty()) {
                "Domain layer must use FeatureFlagProvider.isEnabled / .observe, " +
                    "not the Composable rememberFlag extension:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("feature flag consumption in data layer") {
        Then("data classes must not reference rememberFlag") {
            val dataFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/data..") &&
                        it.resideInPath("..commonMain..")
                }

            val violators = dataFiles.filter { it.text.contains("rememberFlag") }
                .map { "  ${it.name}" }

            assert(violators.isEmpty()) {
                "Data layer must use FeatureFlagProvider.isEnabled / .observe, " +
                    "not the Composable rememberFlag extension:\n${violators.joinToString("\n")}"
            }
        }
    }
})
