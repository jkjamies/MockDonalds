package com.mockdonalds.app.konsist.circuit

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates the Compose UI entry points in `impl/presentation/androidMain`.
 *
 * Two gaps this closes:
 *
 * 1. **Un-annotated screen entry points.** `UiTestConventionsTest` establishes UI test
 *    coverage by *filtering* for `@CircuitInject` composables. A `*Ui` entry point missing
 *    that annotation is therefore not flagged — it is silently dropped from the coverage
 *    check and from Circuit's generated factory, and the screen renders as a blank fallback
 *    at runtime. Absence of an annotation must fail loudly, not narrow a filter.
 *
 * 2. **Leaked helper composables.** `impl/presentation` is private implementation, but
 *    `composeApp` consumes it via `api(project(...))`, so a public helper composable becomes
 *    app-wide API surface. `VisibilityConventionsTest` already enforces the equivalent rule
 *    for domain modules; this is the presentation counterpart.
 */
class UiCompositionConventionsTest : BehaviorSpec({

    val uiFiles = Konsist.scopeFromProject()
        .files
        .filter {
            it.resideInPath("..features..") &&
                it.resideInPath("..impl/presentation..") &&
                it.nameWithExtension.endsWith("Ui.kt")
        }

    Given("screen entry points") {
        Then("the composable matching the file name should carry @CircuitInject") {
            val violators = uiFiles.mapNotNull { file ->
                val entryPointName = file.name.removeSuffix(".kt")
                val entryPoint = file.functions().firstOrNull { it.name == entryPointName }
                when {
                    entryPoint == null ->
                        "  ${file.name}: no `$entryPointName` composable found"

                    !entryPoint.hasAnnotation { it.name == "CircuitInject" } ->
                        "  ${file.name}: `$entryPointName` is missing @CircuitInject"

                    else -> null
                }
            }

            assert(violators.isEmpty()) {
                "Every *Ui.kt must expose a composable named after the file, annotated with " +
                    "@CircuitInject — without it the screen is dropped from Circuit's generated " +
                    "factory and from UI test coverage enforcement:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("helper composable visibility") {
        Then("composables other than the screen entry point should be private") {
            val violators = uiFiles.flatMap { file ->
                val entryPointName = file.name.removeSuffix(".kt")
                file.functions()
                    .filter { it.hasAnnotation { a -> a.name == "Composable" } }
                    .filter { it.name != entryPointName }
                    .filter { !it.hasPrivateModifier && !it.hasInternalModifier }
                    .map { "  ${file.name}: ${it.name}" }
            }

            assert(violators.isEmpty()) {
                "Helper composables in impl/presentation must be private or internal — " +
                    "composeApp depends on presentation via `api(project(...))`, so a public " +
                    "helper leaks into app-wide API surface:\n${violators.joinToString("\n")}"
            }
        }
    }
})
