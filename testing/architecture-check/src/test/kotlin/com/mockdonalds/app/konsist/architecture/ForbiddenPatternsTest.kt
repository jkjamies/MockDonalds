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
                    // else under composeApp/iosMain gets this exemption.
                    !it.name.startsWith("CircuitPresenterKotlinBridge")
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
                .filter { !it.name.endsWith("Ui.kt") }
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
            // Allow Compose Multiplatform (androidx.compose.*) — these are KMP, not Android-only.
            // Block actual Android platform imports (android.*, androidx.lifecycle.*, etc.)
            val allowedAndroidxPrefixes = listOf(
                "androidx.compose.",
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
                "Android platform imports are not allowed in commonMain (Compose Multiplatform is fine):\n${violators.joinToString("\n")}"
            }
        }
    }
})
