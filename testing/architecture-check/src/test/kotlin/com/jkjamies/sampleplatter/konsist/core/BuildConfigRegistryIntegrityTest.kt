package com.jkjamies.sampleplatter.konsist.core

import com.lemonappdev.konsist.api.Konsist
import com.jkjamies.sampleplatter.konsist.normalizedPath
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Locks down the KSP-generated build-config registry so 150+ engineers can't accidentally
 * regress it. The @DebugConfigField annotation and the asFields() delegation are the two
 * surfaces that, if misused, would silently desync the debug menu from AppBuildConfig.
 */
class BuildConfigRegistryIntegrityTest : BehaviorSpec({

    Given("the @DebugConfigField annotation contract") {
        val scope = Konsist.scopeFromProject()

        // AST-based: find every property declaration (interface or class) carrying DebugConfigField.
        // Avoids string-match false positives from test error messages or docs.
        val propertiesWithAnnotation = scope.interfaces()
            .flatMap { it.properties() }
            .plus(scope.classes().flatMap { it.properties() })
            .filter { prop -> prop.annotations.any { it.name.endsWith("DebugConfigField") } }

        Then("@DebugConfigField is only used on properties of AppBuildConfig") {
            val violators = propertiesWithAnnotation.filterNot { prop ->
                normalizedPath(prop.containingFile.path).endsWith("/AppBuildConfig.kt")
            }

            assert(violators.isEmpty()) {
                val names = violators.joinToString("\n") { "  ${it.name} in ${it.containingFile.path}" }
                "@DebugConfigField is only valid on AppBuildConfig properties. Other uses break the " +
                    "BuildConfigRegistryProcessor contract (which only scans AppBuildConfig):\n$names"
            }
        }
    }

    Given("the BuildConfigField.asFields() delegation") {
        val scope = Konsist.scopeFromProject()

        val fieldsFile = scope.files
            .firstOrNull {
                normalizedPath(it.path).contains("/core/build-config/api/") &&
                    normalizedPath(it.path).endsWith("/BuildConfigField.kt")
            }

        Then("BuildConfigField.kt exists") {
            assert(fieldsFile != null) { "BuildConfigField.kt not found under core/build-config/api" }
        }

        Then("asFields() must be a pure delegation to the generated registry — no hand-rolled list") {
            val text = fieldsFile!!.text
            assert(text.contains("= generatedFields()")) {
                "BuildConfigField.asFields() must delegate to the KSP-generated generatedFields() extension. " +
                    "Reverting to a hand-rolled listOf(…) defeats the processor and will silently drift."
            }
            assert(!text.contains("listOf(")) {
                "BuildConfigField.kt must not contain a hand-rolled listOf(…). The registry is generated — " +
                    "add @DebugConfigField to the new property on AppBuildConfig instead."
            }
        }

        Then("no hand-written generatedFields function may shadow the KSP output") {
            val hand = scope.files.filter { file ->
                val path = normalizedPath(file.path)
                val isGenerated = path.contains("/build/generated/")
                !isGenerated &&
                    path.contains("/core/build-config/") &&
                    file.text.contains("fun AppBuildConfig.generatedFields")
            }

            assert(hand.isEmpty()) {
                val names = hand.joinToString("\n") { "  ${it.name} (${it.path})" }
                "A hand-written AppBuildConfig.generatedFields() would collide with the KSP output. " +
                    "Delete it — the processor in :build-tooling:ksp-build-config-registry owns this function:\n$names"
            }
        }
    }

    Given("the FakeAppBuildConfig generator contract") {
        val scope = Konsist.scopeFromProject()

        Then("no hand-written FakeAppBuildConfig may shadow the KSP output") {
            val hand = scope.classes().filter { cls ->
                val path = normalizedPath(cls.containingFile.path)
                val isGenerated = path.contains("/build/generated/")
                !isGenerated &&
                    cls.name == "FakeAppBuildConfig" &&
                    path.contains("/core/build-config/test/")
            }

            assert(hand.isEmpty()) {
                val names = hand.joinToString("\n") { "  ${it.name} (${it.containingFile.path})" }
                "A hand-written FakeAppBuildConfig would collide with the KSP-generated class. " +
                    "Delete it — the processor in :build-tooling:ksp-fake-app-build-config owns this type. " +
                    "Tests mutate the generated fake directly (every property is an override var):\n$names"
            }
        }
    }
})
