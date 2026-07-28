package com.jkjamies.sampleplatter.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec
import java.io.File

/**
 * Validates that core:strings stays a resources-only Android module and that
 * features never declare a direct dependency on it — the
 * `sampleplatter.kmp.presentation` convention plugin auto-wires it onto every
 * presentation module's `androidMain` source set.
 *
 * Catches two drift modes:
 * - Adding Kotlin code under `core/strings/` (the module is meant to expose
 *   only generated `R.string.*` resources from Phrase).
 * - Re-declaring `:core:strings` in a feature `build.gradle.kts`, which makes
 *   future plugin-side wiring changes impossible to roll out cleanly.
 */
class CoreStringsConventionsTest : BehaviorSpec({

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

    Given("core:strings module shape") {
        Then("core:strings must not contain any Kotlin source files") {
            val stringsSrc = projectRoot.resolve("core/strings/src")
            val kotlinFiles = if (stringsSrc.exists()) {
                stringsSrc.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .toList()
            } else {
                emptyList()
            }

            assert(kotlinFiles.isEmpty()) {
                "core:strings is resources-only — Phrase-generated `R.string.*` is its " +
                    "entire public API. Move Kotlin code elsewhere:\n" +
                    kotlinFiles.joinToString("\n") { "  ${it.relativeTo(projectRoot)}" }
            }
        }

        Then("core:strings must only have an androidMain source set") {
            val srcDir = projectRoot.resolve("core/strings/src")
            val sourceSets = if (srcDir.exists()) {
                srcDir.listFiles().orEmpty().filter { it.isDirectory }.map { it.name }
            } else {
                emptyList()
            }
            val unexpected = sourceSets.filterNot { it == "androidMain" }

            assert(unexpected.isEmpty()) {
                "core:strings must remain Android-only so the iOS framework export " +
                    "stays untouched. Found unexpected source sets: $unexpected"
            }
        }
    }

    Given("feature dependency declarations") {
        Then("no feature build.gradle.kts may declare :core:strings — the presentation plugin auto-wires it") {
            val featuresDir = projectRoot.resolve("features")
            val violators = if (featuresDir.exists()) {
                featuresDir.walkTopDown()
                    .filter { it.isFile && it.name == "build.gradle.kts" }
                    .filter { it.readText().contains(":core:strings") }
                    .map { it.relativeTo(projectRoot).path }
                    .toList()
            } else {
                emptyList()
            }

            assert(violators.isEmpty()) {
                "Feature build.gradle.kts files must not declare :core:strings — " +
                    "the `sampleplatter.kmp.presentation` plugin adds it to androidMain " +
                    "for every feature automatically:\n" +
                    violators.joinToString("\n") { "  $it" }
            }
        }
    }
})
