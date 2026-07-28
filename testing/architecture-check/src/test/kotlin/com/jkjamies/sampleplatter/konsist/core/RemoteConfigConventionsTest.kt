package com.jkjamies.sampleplatter.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces remote-config consumption patterns:
 * - Presenters must read flags via `RemoteConfigProvider.rememberFlag(flag)` and typed
 *   configs via `RemoteConfigProvider.rememberConfig(config)` — never call
 *   `.isEnabled(...)`, `.observe(...)`, `.getConfig(...)`, or `.observeConfig(...)`
 *   directly (those bypass Compose state and won't react to changes).
 * - Domain and data layers must use the suspend/flow forms (`isEnabled` / `observe`
 *   / `getConfig` / `observeConfig`) — never `rememberFlag` or `rememberConfig`
 *   (those are Composable and presenter-only).
 */
class RemoteConfigConventionsTest : BehaviorSpec({

    Given("remote-config consumption in presentation layer") {
        Then("presenters must not call provider read methods directly") {
            val presenterFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/presentation..") &&
                        it.resideInPath("..commonMain..") &&
                        it.imports.any { import ->
                            import.name == "com.jkjamies.sampleplatter.core.remoteconfig.RemoteConfigProvider"
                        }
                }

            val violators = presenterFiles.filter { file ->
                file.text.contains(".isEnabled(") ||
                    file.text.contains(".observe(") ||
                    file.text.contains(".getConfig(") ||
                    file.text.contains(".observeConfig(")
            }.map { "  ${it.name}" }

            assert(violators.isEmpty()) {
                "Presenters must read via RemoteConfigProvider.rememberFlag(flag) / rememberConfig(config), " +
                    "not direct isEnabled / observe / getConfig / observeConfig calls:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("remote-config consumption in domain layer") {
        Then("domain classes must not reference rememberFlag or rememberConfig") {
            val domainFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/domain..") &&
                        it.resideInPath("..commonMain..") &&
                        !it.resideInPath("..api..")
                }

            val violators = domainFiles.filter {
                it.text.contains("rememberFlag") || it.text.contains("rememberConfig")
            }.map { "  ${it.name}" }

            assert(violators.isEmpty()) {
                "Domain layer must use RemoteConfigProvider.isEnabled / observe / getConfig / observeConfig, " +
                    "not the Composable rememberFlag / rememberConfig extensions:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("remote-config consumption in data layer") {
        Then("data classes must not reference rememberFlag or rememberConfig") {
            val dataFiles = Konsist.scopeFromProject()
                .files
                .filter {
                    it.resideInPath("..impl/data..") &&
                        it.resideInPath("..commonMain..")
                }

            val violators = dataFiles.filter {
                it.text.contains("rememberFlag") || it.text.contains("rememberConfig")
            }.map { "  ${it.name}" }

            assert(violators.isEmpty()) {
                "Data layer must use RemoteConfigProvider.isEnabled / observe / getConfig / observeConfig, " +
                    "not the Composable rememberFlag / rememberConfig extensions:\n${violators.joinToString("\n")}"
            }
        }
    }
})
