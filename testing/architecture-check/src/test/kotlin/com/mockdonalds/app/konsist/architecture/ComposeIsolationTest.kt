package com.mockdonalds.app.konsist.architecture

import com.lemonappdev.konsist.api.Konsist
import com.mockdonalds.app.konsist.normalizedPath
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces Compose isolation: only presentation-layer modules may import Compose.
 *
 * Allowed locations (Compose-aware by design):
 * - features/{name}/impl/presentation/...   — feature presenters + Compose UI
 * - core/presentation/...                   — cross-cutting Compose helpers (rememberFlag etc.)
 * - core/theme/...                          — Compose theme
 * - core/circuit/...                        — Compose-aware Circuit integration
 * - composeApp/...                          — KMP Compose entry
 * - androidApp/...                          — Android entry
 * - testing/navint-tests/...                — navigation/integration tests host real Compose UI
 *
 * Forbidden everywhere else: api/domain, api/navigation, impl/domain, impl/data, feature test/
 * fakes, all non-presentation core modules, and the rest of testing/. Compose creeping into those
 * layers means the api/impl boundary is leaking and consumers pay an unnecessary Compose cost.
 */
class ComposeIsolationTest : BehaviorSpec({

    val composePackagePrefixes = listOf(
        "androidx.compose.",
        "org.jetbrains.compose.",
    )

    val allowedPathSubstrings = listOf(
        "/features/" to "/impl/presentation/",
        "/core/presentation/" to null,
        "/core/theme/" to null,
        "/core/circuit/" to null,
        "/composeApp/" to null,
        "/androidApp/" to null,
        "/testing/navint-tests/" to null,
    )

    Given("Compose imports across the project") {
        Then("they must only appear in allowed presentation-layer locations") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { file ->
                    file.imports.any { import ->
                        composePackagePrefixes.any { prefix -> import.name.startsWith(prefix) }
                    }
                }
                .filterNot { file ->
                    val path = normalizedPath(file.path)
                    allowedPathSubstrings.any { (first, second) ->
                        if (second == null) path.contains(first) else path.contains(first) && path.contains(second)
                    }
                }
                .map { "  ${it.path}" }

            assert(violators.isEmpty()) {
                "Compose is restricted to presentation-layer modules. " +
                    "Move Compose-dependent code to features/*/impl/presentation, core:presentation, " +
                    "core:theme, core:circuit, composeApp, androidApp, or testing/navint-tests. " +
                    "Violators:\n${violators.joinToString("\n")}"
            }
        }
    }
})
