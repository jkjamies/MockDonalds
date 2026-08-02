package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import com.mockdonalds.app.konsist.normalizedPath
import io.kotest.core.spec.style.BehaviorSpec

class BuildConfigCoverageTest : BehaviorSpec({

    Given("AppBuildConfig public facade") {
        val scope = Konsist.scopeFromProject()

        val buildConfigFiles = scope.files.filter { normalizedPath(it.path).contains("/core/build-config/") }

        val appBuildConfig = buildConfigFiles.firstOrNull { it.path.endsWith("AppBuildConfig.kt") }
        val testFile = buildConfigFiles.firstOrNull { it.path.endsWith("AppBuildConfigTest.kt") }
        val fieldsFile = buildConfigFiles.firstOrNull { it.path.endsWith("BuildConfigField.kt") }
        val annotationFile = buildConfigFiles.firstOrNull { it.path.endsWith("DebugConfigField.kt") }

        Then("the facade, its test file, the enumeration file, and the annotation must exist") {
            assert(appBuildConfig != null) { "AppBuildConfig.kt not found in core:build-config:api" }
            assert(testFile != null) { "AppBuildConfigTest.kt not found in core:build-config:impl" }
            assert(fieldsFile != null) { "BuildConfigField.kt not found in core:build-config:api" }
            assert(annotationFile != null) { "DebugConfigField.kt not found in core:build-config:api" }
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

        Then("every AppBuildConfig property must carry @DebugConfigField so the KSP registry can enumerate it") {
            val facade = appBuildConfig!!

            val properties = facade.interfaces(includeNested = false)
                .firstOrNull { it.name == "AppBuildConfig" }
                ?.properties()
                .orEmpty()

            val missing = properties.filterNot { prop ->
                prop.annotations.any { it.name.endsWith("DebugConfigField") }
            }.map { it.name }

            assert(missing.isEmpty()) {
                "AppBuildConfig properties without @DebugConfigField: ${missing.joinToString()}. " +
                    "The KSP registry processor generates asFields() from this annotation; missing it fails " +
                    "compilation with a generic KSP error. Annotating here gives a direct Konsist failure first."
            }
        }
    }
})
