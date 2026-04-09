package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates that core:metro maintains proper isolation:
 * - Must not import from any feature module
 * - Must not import from impl/ modules
 */
class CoreMetroConventionsTest : BehaviorSpec({

    Given("core:metro isolation") {
        Then("core:metro should not import from any feature module") {
            val metroFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..core/metro..") && it.resideInPath("..commonMain..") }

            val violators = metroFiles.filter { file ->
                file.imports.any { it.name.contains(".features.") }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val badImports = file.imports.filter { it.name.contains(".features.") }
                    "  ${file.name}: ${badImports.joinToString { it.name }}"
                }
                "core:metro must not import from feature modules:\n$details"
            }
        }

        Then("core:metro should not import from impl modules") {
            val metroFiles = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..core/metro..") && it.resideInPath("..commonMain..") }

            val violators = metroFiles.filter { file ->
                file.imports.any { imp ->
                    imp.name.contains(".impl.") || imp.name.contains(".data.") || imp.name.contains(".presentation.")
                }
            }

            assert(violators.isEmpty()) {
                val details = violators.joinToString("\n") { file ->
                    val badImports = file.imports.filter { imp ->
                        imp.name.contains(".impl.") || imp.name.contains(".data.") || imp.name.contains(".presentation.")
                    }
                    "  ${file.name}: ${badImports.joinToString { it.name }}"
                }
                "core:metro must not import from impl modules:\n$details"
            }
        }
    }
})
