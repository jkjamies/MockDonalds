package com.mockdonalds.app.konsist.circuit

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec
import java.io.File

/**
 * Enforces Android ↔ iOS screen parity.
 *
 * Every screen wired on one platform must be wired on the other. Nothing else enforced this:
 * Konsist only ever looked at Kotlin, Harmonize's `ViewConventionsTest` validates the SwiftUI
 * views that happen to exist, and the runtime falls back to
 * `Text("No UI for screen: …")` in `Circuit.swift` rather than failing. The result was that a
 * feature could pass the entire architecture suite on both platforms and still render a
 * placeholder string on iOS.
 *
 * The pairing is by `Screen` type:
 * - Kotlin: `@CircuitInject({Screen}::class, AppScope::class)` on the `*Ui` composable
 * - Swift:  `@CircuitInject({Screen}.self, {Screen}UiState.self)` on the `*View` struct
 *
 * This is the rule that would have caught an `/add-feature` scaffold shipping Android-only.
 */
class PlatformParityTest : BehaviorSpec({

    val projectRoot = Konsist.scopeFromProject()
        .files
        .first()
        .path
        .let { path ->
            var dir = File(path).parentFile
            while (dir != null && !dir.resolve("settings.gradle.kts").exists()) {
                dir = dir.parentFile
            }
            dir ?: error("Could not find project root (settings.gradle.kts) from $path")
        }

    val kotlinScreens: Set<String> = Konsist.scopeFromProject()
        .files
        .filter { it.resideInPath("..features..") && it.resideInPath("..impl/presentation..") }
        .flatMap { file ->
            Regex("""@CircuitInject\(\s*(\w+)::class""")
                .findAll(file.text)
                .map { it.groupValues[1] }
                .toList()
        }
        .toSet()

    val swiftViewsDir = projectRoot.resolve("iosApp/iosApp/Features")

    val swiftScreens: Set<String> = if (swiftViewsDir.exists()) {
        swiftViewsDir.walkTopDown()
            .filter { it.isFile && it.extension == "swift" }
            .flatMap { file ->
                Regex("""@CircuitInject\(\s*(\w+)\.self""")
                    .findAll(file.readText())
                    .map { it.groupValues[1] }
            }
            .toSet()
    } else {
        emptySet()
    }

    Given("screen registration parity") {

        When("both platforms' @CircuitInject registrations are collected") {

            Then("the project should declare at least one screen on each platform") {
                // Guards against the rule silently passing because a path assumption broke —
                // an empty-set comparison is vacuously true and would hide every real violation.
                assert(kotlinScreens.isNotEmpty()) {
                    "Found no Kotlin @CircuitInject screens — the scan path is wrong, not the code"
                }
                assert(swiftScreens.isNotEmpty()) {
                    "Found no Swift @CircuitInject views under ${swiftViewsDir.path} — " +
                        "the scan path is wrong, not the code"
                }
            }

            Then("every Kotlin screen should have a matching SwiftUI view") {
                val missing = (kotlinScreens - swiftScreens).sorted()

                assert(missing.isEmpty()) {
                    "Screens wired on Android but not on iOS — each needs a " +
                        "`iosApp/iosApp/Features/{Feature}/{Screen}View.swift` carrying " +
                        "`@CircuitInject({Screen}.self, {Screen}UiState.self)`, or it renders as " +
                        "the \"No UI for screen\" fallback:\n" +
                        missing.joinToString("\n") { "  $it" }
                }
            }

            Then("every SwiftUI view should have a matching Kotlin screen") {
                val orphaned = (swiftScreens - kotlinScreens).sorted()

                assert(orphaned.isEmpty()) {
                    "SwiftUI views registered for screens with no Kotlin presenter — " +
                        "these can never resolve a presenter at runtime:\n" +
                        orphaned.joinToString("\n") { "  $it" }
                }
            }
        }
    }
})
