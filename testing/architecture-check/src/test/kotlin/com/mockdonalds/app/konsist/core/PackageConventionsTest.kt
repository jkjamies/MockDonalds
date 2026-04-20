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
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && it.resideInPath("..commonMain..") }
                .assertTrue { file ->
                    // Extract feature name from path: features/home/api -> "home". Hyphens in
                    // directory names (e.g. "debug-menu") are stripped in Kotlin packages
                    // (debugmenu) since `-` isn't a valid package-segment character.
                    val featureName = file.path.substringAfter("features/").substringBefore("/")
                    val packageSegment = featureName.replace("-", "")
                    file.packagee?.name?.contains(".features.$packageSegment.") == true
                }
        }
    }
})
