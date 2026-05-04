package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates package naming and structure conventions across the project.
 */
class PackageConventionsTest : BehaviorSpec({

    Given("feature module packages") {
        Then("all feature commonMain source files should follow com.mockdonalds.app.features.* package convention") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..commonMain..") }
                .assertTrue {
                    it.packagee?.name?.startsWith("com.mockdonalds.app.features.") == true
                }
        }
    }

    Given("core module packages") {
        Then("all core commonMain source files should follow com.mockdonalds.app.core.* package convention") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..core..") && it.resideInPath("..commonMain..") }
                .assertTrue {
                    it.packagee?.name?.startsWith("com.mockdonalds.app.core.") == true
                }
        }
    }

    Given("module naming alignment") {
        Then("feature files should have package segments matching their module path") {
            // Path layout: `features/{grouping}/{name}/{api|impl|test}/...`
            //   grouping = mobile | kiosk | shared
            //   name     = feature directory (hyphens stripped in package — e.g., debug-menu → debugmenu)
            // Expected package contains: `.features.{grouping}.{name}.` so callsites read
            // `com.mockdonalds.app.features.mobile.home.*` / `com.mockdonalds.app.features.kiosk.attract.*` /
            // `com.mockdonalds.app.features.shared.*` and the grouping is visible in every import.
            val knownGroupings = setOf("mobile", "kiosk", "shared")
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..commonMain..") }
                .assertTrue { file ->
                    val pathAfterFeatures = file.path.substringAfter("features/")
                    val grouping = pathAfterFeatures.substringBefore("/")
                    if (grouping !in knownGroupings) return@assertTrue false
                    val featureName = pathAfterFeatures.substringAfter("/").substringBefore("/")
                    val packageSegment = featureName.replace("-", "")
                    val expected = ".features.$grouping.$packageSegment."
                    file.packagee?.name?.contains(expected) == true
                }
        }
    }

    Given("testing module packages") {
        // Testing modules don't use full path-package mirroring (would force a silly
        // `com.mockdonalds.kiosk.kiosk.*` doubling for kiosk). Instead, each testing
        // module belongs to its app's top-level namespace:
        //   testing/mobile/**          → com.mockdonalds.app.*
        //   testing/kiosk/**           → com.mockdonalds.kiosk.*
        //   testing/architecture-check → com.mockdonalds.app.* (Konsist scans the project)
        Then("testing/mobile/** files must use the consumer namespace") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing/mobile/..") }
                .assertTrue { it.packagee?.name?.startsWith("com.mockdonalds.app.") == true }
        }

        Then("testing/kiosk/** files must use the kiosk namespace") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing/kiosk/..") }
                .assertTrue { it.packagee?.name?.startsWith("com.mockdonalds.kiosk.") == true }
        }

        Then("testing/architecture-check/** files must use the konsist namespace") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..testing/architecture-check/..") }
                .assertTrue { it.packagee?.name?.startsWith("com.mockdonalds.app.konsist") == true }
        }
    }
})
