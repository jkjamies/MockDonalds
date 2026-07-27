package com.mockdonalds.app.konsist.architecture

import com.lemonappdev.konsist.api.Konsist
import com.mockdonalds.app.konsist.isProductionSourcePath
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Enforces forbidden patterns that are architectural violations in this project.
 * No ViewModels, no raw CoroutineScope/Dispatchers, no app module imports from library modules.
 *
 * Scope: all production source sets (`commonMain`, `androidMain`, `iosMain`). The coroutine
 * rules in particular are worthless when scoped to `commonMain` only — every Compose UI file
 * in the project lives in `androidMain`, so that is precisely where a stray `GlobalScope` or
 * hardcoded `Dispatchers.IO` would appear.
 */
class ForbiddenPatternsTest : BehaviorSpec({

    Given("no ViewModels") {
        Then("no class should extend ViewModel or AndroidViewModel") {
            val violators = Konsist.scopeFromProject()
                .classes()
                .filter {
                    it.hasParent { p ->
                        p.name == "ViewModel" || p.name == "AndroidViewModel"
                    }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "ViewModels are not allowed — this project uses Circuit presenters for state management:\n$names"
            }
        }

        Then("no file should import ViewModel classes") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { file ->
                    file.imports.any {
                        it.name.contains("androidx.lifecycle.ViewModel") ||
                            it.name.contains("androidx.lifecycle.AndroidViewModel")
                    }
                }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} (${it.path})" }
                "ViewModel imports are not allowed — use Circuit presenters instead:\n$names"
            }
        }
    }

    Given("CenterPost only — no raw coroutine scope or hardcoded dispatchers") {
        val featureAndPresenterFiles = Konsist.scopeFromProject()
            .files
            .filter {
                isProductionSourcePath(it.path) &&
                    (it.resideInPath("..features..") || it.resideInPath("..composeApp..")) &&
                    // CenterPost is the one place allowed to own a CoroutineScope on Android.
                    !it.resideInPath("..centerpost..") &&
                    // CircuitPresenterKotlinBridge is its iOS counterpart: Molecule's
                    // `launchMolecule` requires a CoroutineScope to host the presenter
                    // composition, exactly as Compose's recomposer does on Android. Nothing
                    // else under composeApp/iosMain gets this exemption — and the exemption is
                    // pinned to that one path, so a same-named file dropped into a feature
                    // module (or into commonMain) does not inherit it.
                    !(
                        it.name.startsWith("CircuitPresenterKotlinBridge") &&
                            it.resideInPath("..composeApp..") &&
                            it.resideInPath("..iosMain..")
                        )
            }

        Then("feature modules should not directly use CoroutineScope") {
            val violators = featureAndPresenterFiles
                .flatMap { file ->
                    file.imports
                        .filter {
                            it.name == "kotlinx.coroutines.CoroutineScope" ||
                                it.name == "kotlinx.coroutines.MainScope" ||
                                it.name == "kotlinx.coroutines.GlobalScope"
                        }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Direct CoroutineScope usage is not allowed in features — use CenterPost (rememberCenterPost) instead:\n${violators.joinToString("\n")}"
            }
        }

        Then("feature modules should not directly use launch or async") {
            // `*Ui.kt` is exempt: UI-local coroutines (scroll animation, snackbar dismissal)
            // are driven by Compose's own `rememberCoroutineScope()` and are not business
            // logic, so routing them through CenterPost would be wrong. The exemption is
            // narrow — the CoroutineScope and Dispatchers rules above still apply to UI files.
            val violators = featureAndPresenterFiles
                // `path`, not `name`: Konsist's file `name` carries no extension, so
                // `name.endsWith("Ui.kt")` matched nothing and this carve-out never applied.
                // No UI file trips the rule today, so it passed either way — but the exemption
                // would not have fired the first time someone added a legitimate one.
                .filter { !it.path.endsWith("Ui.kt") }
                .flatMap { file ->
                    file.imports
                        .filter {
                            it.name == "kotlinx.coroutines.launch" ||
                                it.name == "kotlinx.coroutines.async"
                        }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Direct launch/async is not allowed in features — use CenterPost instead:\n${violators.joinToString("\n")}"
            }
        }

        Then("feature modules should not hardcode Dispatchers") {
            val violators = featureAndPresenterFiles
                .flatMap { file ->
                    file.imports
                        .filter {
                            it.name == "kotlinx.coroutines.Dispatchers" ||
                                it.name.startsWith("kotlinx.coroutines.Dispatchers.")
                        }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Hardcoded Dispatchers are not allowed — use CenterPostDispatchers (injected) instead:\n${violators.joinToString("\n")}"
            }
        }

    }

    Given("app module isolation") {
        Then("feature modules should not import from androidApp or composeApp") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") }
                .flatMap { file ->
                    file.imports
                        .filter {
                            it.name.contains(".androidApp.") ||
                                it.name.contains("com.mockdonalds.app.android.")
                        }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Feature modules must not import from app modules:\n${violators.joinToString("\n")}"
            }
        }

        Then("core modules should not import from androidApp or composeApp") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..core..") }
                .flatMap { file ->
                    file.imports
                        .filter {
                            it.name.contains(".androidApp.") ||
                                it.name.contains("com.mockdonalds.app.android.")
                        }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Core modules must not import from app modules:\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("no Android platform framework in shared code") {
        Then("commonMain source sets should not import android platform packages") {
            // Allow the Compose *runtime* (KMP, used by Molecule on iOS) but not Compose UI —
            // see the dedicated rule below for why. Block actual Android platform imports
            // (android.*, androidx.lifecycle.*, etc.)
            val allowedAndroidxPrefixes = listOf(
                "androidx.compose.runtime.",
                "androidx.annotation.",
                "androidx.collection.",
            )

            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..commonMain..") }
                .filter { !it.resideInPath("..androidApp..") && !it.resideInPath("..composeApp..") }
                .flatMap { file ->
                    file.imports
                        .filter { import ->
                            val name = import.name
                            (name.startsWith("android.") ||
                                (name.startsWith("androidx.") && allowedAndroidxPrefixes.none { name.startsWith(it) }))
                        }
                        .map { "  ${file.name}: ${it.name}" }
                }

            assert(violators.isEmpty()) {
                "Android platform imports are not allowed in commonMain " +
                    "(the Compose runtime is fine — Compose UI is not):\n${violators.joinToString("\n")}"
            }
        }
    }

    Given("Compose runtime, not Compose UI, in shared code") {
        // The project's central architectural claim is that iOS uses the Compose *runtime*
        // only — Molecule runs presenter composables to produce state, and SwiftUI renders.
        // Nothing enforced it. Anything in commonMain is compiled for iosX64, iosArm64 and
        // iosSimulatorArm64 on every build, so a single Compose UI import in commonMain drags
        // the material3/foundation/ui stack into the iOS framework for code iOS can never
        // reach. That is exactly how `composeApp` came to declare compose.foundation,
        // compose.material3, compose.ui and compose.components.resources in commonMain while
        // having zero `androidx.compose` imports there, and how the whole Kotlin design system
        // ended up in `core:theme/commonMain` with every consumer in androidMain.
        //
        // Compose UI belongs in androidMain. The `mockdonalds.kmp.presentation` convention
        // plugin already wires it that way; this rule stops modules drifting off it.
        val composeUiPrefixes = listOf(
            "androidx.compose.ui.",
            "androidx.compose.foundation.",
            "androidx.compose.material",
            "androidx.compose.animation.",
        )

        Then("commonMain should not import Compose UI") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..commonMain..") }
                .flatMap { file ->
                    file.imports
                        .filter { import -> composeUiPrefixes.any { import.name.startsWith(it) } }
                        .map { "  ${file.name}: ${it.name} (${file.path})" }
                }

            assert(violators.isEmpty()) {
                "Compose UI is not allowed in commonMain — move the file (and the dependency) " +
                    "to androidMain. iOS renders with SwiftUI and uses the Compose runtime only, " +
                    "so Compose UI in commonMain is compiled into the iOS framework for nothing:\n" +
                    violators.joinToString("\n")
            }
        }

        Then("iosMain should not import Compose UI") {
            val violators = Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..iosMain..") }
                .flatMap { file ->
                    file.imports
                        .filter { import -> composeUiPrefixes.any { import.name.startsWith(it) } }
                        .map { "  ${file.name}: ${it.name} (${file.path})" }
                }

            assert(violators.isEmpty()) {
                "Compose UI is not allowed in iosMain — every iOS view is SwiftUI. " +
                    "The Compose runtime (androidx.compose.runtime.*) is the only permitted " +
                    "Compose dependency on iOS:\n${violators.joinToString("\n")}"
            }
        }
    }
})
