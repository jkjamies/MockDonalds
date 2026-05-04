package com.mockdonalds.app.konsist.kiosk

import io.kotest.core.spec.style.BehaviorSpec
import java.io.File

/**
 * Belt-and-suspenders for the cross-host import boundary.
 *
 * `KioskBoundaryTest` already enforces that no Kotlin source file imports across
 * the mobile↔kiosk boundary. This test additionally enforces that no Gradle build
 * script declares a project dependency across that boundary — even if the dep
 * isn't actually used by any source file (i.e., a dead dep that R8 would strip).
 *
 * The rationale: a phantom dep makes intent ambiguous, slows configuration, and
 * lets a future contributor "land an import" of the wrong-host symbol with a
 * one-line edit instead of two. Forbidding the dep at the build-script level
 * makes the boundary maintained at a single edit-time gate.
 *
 * | Build script lives in...           | Must NOT reference Gradle path... |
 * |------------------------------------|-----------------------------------|
 * | `composeApp/`                      | `:features:kiosk:*`               |
 * | `androidApp/`                      | `:features:kiosk:*`               |
 * | `features/mobile/...`              | `:features:kiosk:*`               |
 * | `testing/mobile/...`               | `:features:kiosk:*`               |
 * | `kioskApp/`                        | `:features:mobile:*`              |
 * | `kioskComposeApp/`                 | `:features:mobile:*`              |
 * | `features/kiosk/...`               | `:features:mobile:*`              |
 * | `testing/kiosk/...`                | `:features:mobile:*`              |
 * | `features/shared/...`              | both `:features:mobile:*` and `:features:kiosk:*` |
 *
 * Konsist normally scans Kotlin source files; here we read the raw `.kts` text
 * (Konsist's File model exposes the `text` property) so we can grep for dependency
 * declarations without parsing them as full Kotlin programs.
 */
class CrossHostGradleDepTest : BehaviorSpec({

    val projectRoot = run {
        var dir: File? = File(".").canonicalFile
        while (dir != null && !dir.resolve("settings.gradle.kts").exists()) {
            dir = dir.parentFile
        }
        dir ?: error("Could not locate project root from ${File(".").canonicalPath}")
    }

    fun gradleScripts(matchingPath: String): List<File> {
        val regex = matchingPath.toRegex()
        return projectRoot.walkTopDown()
            .filter { it.isFile && it.name == "build.gradle.kts" }
            .filter { !it.path.contains("/build/") && !it.path.contains("/.git/") }
            .filter { regex.containsMatchIn(it.relativeTo(projectRoot).path) }
            .toList()
    }

    fun violators(scripts: List<File>, forbiddenPrefix: String): List<String> =
        scripts.flatMap { script ->
            val text = script.readText()
            // Match `:features:{prefix}:` references that could appear inside `project(":features:...")`.
            Regex("\":features:$forbiddenPrefix:[^\"]+\"").findAll(text)
                .map { "  ${script.relativeTo(projectRoot).path}: ${it.value}" }
                .toList()
        }

    Given("the consumer host and consumer features") {
        val scripts = gradleScripts("^(composeApp|androidApp|features/mobile/|testing/mobile/)")

        Then("their build scripts must not reference :features:kiosk:* paths") {
            val v = violators(scripts, "kiosk")
            assert(v.isEmpty()) {
                "Consumer-host / mobile-feature / mobile-test build scripts must not declare " +
                    "Gradle deps on kiosk features:\n${v.joinToString("\n")}"
            }
        }
    }

    Given("the kiosk host and kiosk features") {
        val scripts = gradleScripts("^(kioskApp|kioskComposeApp|features/kiosk/|testing/kiosk/)")

        Then("their build scripts must not reference :features:mobile:* paths") {
            val v = violators(scripts, "mobile")
            assert(v.isEmpty()) {
                "Kiosk-host / kiosk-feature / kiosk-test build scripts must not declare " +
                    "Gradle deps on mobile features:\n${v.joinToString("\n")}"
            }
        }
    }

    Given("shared features") {
        val scripts = gradleScripts("^features/shared/")

        Then("their build scripts must not reference :features:mobile:* paths") {
            val v = violators(scripts, "mobile")
            assert(v.isEmpty()) {
                "Shared-feature build scripts must not depend on mobile-specific features:\n" +
                    v.joinToString("\n")
            }
        }

        Then("their build scripts must not reference :features:kiosk:* paths") {
            val v = violators(scripts, "kiosk")
            assert(v.isEmpty()) {
                "Shared-feature build scripts must not depend on kiosk-specific features:\n" +
                    v.joinToString("\n")
            }
        }
    }
})
