package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec

class BuildConfigCoverageTest : BehaviorSpec({

    Given("AppBuildConfig public facade") {
        val scope = Konsist.scopeFromProject()

        val buildConfigFiles = scope.files.filter { it.path.contains("/core/build-config/") }

        val appBuildConfig = buildConfigFiles.firstOrNull { it.path.endsWith("AppBuildConfig.kt") }
        val testFile = buildConfigFiles.firstOrNull { it.path.endsWith("AppBuildConfigTest.kt") }

        Then("both the facade and its test file must exist") {
            assert(appBuildConfig != null) { "AppBuildConfig.kt not found in core:build-config" }
            assert(testFile != null) { "AppBuildConfigTest.kt not found in core:build-config" }
        }

        Then("every AppBuildConfig property must be referenced in AppBuildConfigTest") {
            val facade = appBuildConfig!!
            val test = testFile!!

            val properties = facade.interfaces(includeNested = false)
                .firstOrNull { it.name == "AppBuildConfig" }
                ?.properties()
                ?.map { it.name }
                .orEmpty()

            assert(properties.isNotEmpty()) {
                "AppBuildConfig has no declared properties — schema is empty or parse failed"
            }

            val testText = test.text
            val missing = properties.filterNot { prop ->
                testText.contains("config.$prop") || testText.contains(".$prop")
            }

            assert(missing.isEmpty()) {
                "AppBuildConfigTest is missing assertions for fields: ${missing.joinToString()}. " +
                    "Every property on AppBuildConfig must be exercised by the smoke test so new fields " +
                    "can't ship without coverage."
            }
        }
    }
})
