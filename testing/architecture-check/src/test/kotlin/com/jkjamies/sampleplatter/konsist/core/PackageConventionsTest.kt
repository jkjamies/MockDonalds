package com.jkjamies.sampleplatter.konsist.core

import com.lemonappdev.konsist.api.Konsist
import com.jkjamies.sampleplatter.konsist.featurePackageSegment
import com.jkjamies.sampleplatter.konsist.isProductionSourcePath
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates package naming and structure conventions across the project.
 */
class PackageConventionsTest : BehaviorSpec({

    Given("feature module packages") {
        Then("all feature commonMain source files should follow com.jkjamies.sampleplatter.features.* package convention") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && isProductionSourcePath(it.path) }
                .assertTrue {
                    it.packagee?.name?.startsWith("com.jkjamies.sampleplatter.features.") == true
                }
        }
    }

    Given("core module packages") {
        Then("all core commonMain source files should follow com.jkjamies.sampleplatter.core.* package convention") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..core..") && isProductionSourcePath(it.path) }
                .assertTrue {
                    it.packagee?.name?.startsWith("com.jkjamies.sampleplatter.core.") == true
                }
        }
    }

    Given("module naming alignment") {
        Then("feature files should have package segments matching their module path") {
            Konsist.scopeFromProject()
                .files
                .filter { it.resideInPath("..features..") && isProductionSourcePath(it.path) }
                .assertTrue { file ->
                    // features/home/api -> "home"; features/debug-menu/... -> "debugmenu".
                    // Hyphens are stripped because `-` is not a valid package-segment character.
                    val packageSegment = featurePackageSegment(file.path)
                    file.packagee?.name?.contains(".features.$packageSegment.") == true
                }
        }
    }
})
