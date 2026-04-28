package com.mockdonalds.app.konsist.core

import com.lemonappdev.konsist.api.Konsist
import io.kotest.core.spec.style.BehaviorSpec
import java.io.File
import java.util.Properties

/**
 * Locks down the relationship between three schemas that must stay in lockstep:
 * - The Kotlin facade `AppBuildConfig` (api/commonMain).
 * - The properties baseline `core/build-config/impl/Defaults.properties`.
 * - Every per-market combo `core/build-config/impl/markets/{m}/{m}-{e}.properties`.
 *
 * The full `validateAllMarkets` Gradle task covers value-format rules (URLs,
 * locale, etc.) and runs only in `verify full` / `verify all`. This test catches
 * the structural-drift subset — adding a property to `AppBuildConfig` without
 * adding the matching key to `Defaults.properties`, or shipping a new market
 * directory missing an env that other markets have — and runs in the standard
 * architecture-check pass on every PR.
 *
 * Property-name → properties-key conversion: camelCase → SCREAMING_SNAKE_CASE
 * (e.g., `baseUrl` → `BASE_URL`). The runtime `buildType` value is set by
 * `build.gradle.kts` from the active variant, not the properties file, and is
 * excluded from parity checks.
 */
class BuildConfigSchemaParityTest : BehaviorSpec({

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

    val buildConfigImpl = projectRoot.resolve("core/build-config/impl")
    val defaultsFile = buildConfigImpl.resolve("Defaults.properties")
    val marketsDir = buildConfigImpl.resolve("markets")

    fun camelToScreamingSnake(name: String): String =
        buildString {
            name.forEachIndexed { i, c ->
                if (i > 0 && c.isUpperCase()) append('_')
                append(c.uppercaseChar())
            }
        }

    fun loadProps(file: File): Set<String> {
        assert(file.exists()) {
            "Required properties file missing: ${file.relativeTo(projectRoot)}"
        }
        val props = Properties()
        file.inputStream().use { props.load(it) }
        return props.stringPropertyNames()
    }

    val runtimeOnlyKeys = setOf("BUILD_TYPE")

    Given("Defaults.properties as the schema source of truth") {
        Then("Defaults.properties must exist") {
            assert(defaultsFile.exists()) {
                "Defaults.properties not found at ${defaultsFile.relativeTo(projectRoot)}"
            }
        }

        Then("every AppBuildConfig property has a matching key in Defaults.properties") {
            val facade = Konsist.scopeFromProject()
                .files
                .firstOrNull { it.path.endsWith("/core/build-config/api/src/commonMain/kotlin/com/mockdonalds/app/core/buildconfig/AppBuildConfig.kt") }
            assert(facade != null) { "AppBuildConfig.kt not found in core:build-config:api" }

            val properties = facade!!.interfaces(includeNested = false)
                .firstOrNull { it.name == "AppBuildConfig" }
                ?.properties()
                .orEmpty()

            val expectedKeys = properties
                .map { camelToScreamingSnake(it.name) }
                .filter { it !in runtimeOnlyKeys }
                .toSet()

            val defaultsKeys = loadProps(defaultsFile)
            val missing = expectedKeys - defaultsKeys

            assert(missing.isEmpty()) {
                "AppBuildConfig declares properties whose corresponding keys are absent " +
                    "from Defaults.properties: ${missing.sorted().joinToString()}.\n" +
                    "Either add the key to Defaults.properties (and to every market combo) " +
                    "or, if the value is set by build.gradle.kts only, add the SCREAMING_SNAKE_CASE " +
                    "name to runtimeOnlyKeys in this test."
            }
        }

        Then("every key in Defaults.properties has a matching AppBuildConfig property") {
            val facade = Konsist.scopeFromProject()
                .files
                .firstOrNull { it.path.endsWith("/core/build-config/api/src/commonMain/kotlin/com/mockdonalds/app/core/buildconfig/AppBuildConfig.kt") }
            assert(facade != null) { "AppBuildConfig.kt not found in core:build-config:api" }

            val expectedKeys = facade!!.interfaces(includeNested = false)
                .firstOrNull { it.name == "AppBuildConfig" }
                ?.properties()
                ?.map { camelToScreamingSnake(it.name) }
                .orEmpty()
                .toSet()

            val orphaned = loadProps(defaultsFile) - expectedKeys - runtimeOnlyKeys

            assert(orphaned.isEmpty()) {
                "Defaults.properties declares keys with no matching AppBuildConfig property: " +
                    "${orphaned.sorted().joinToString()}.\n" +
                    "BuildKonfig will emit them but no consumer can read them. " +
                    "Either expose them on AppBuildConfig or remove from Defaults.properties."
            }
        }
    }

    Given("market combo files") {
        val comboFiles = if (marketsDir.exists()) {
            marketsDir.listFiles().orEmpty()
                .filter { it.isDirectory }
                .flatMap { it.listFiles().orEmpty().filter { f -> f.isFile && f.extension == "properties" } }
        } else {
            emptyList()
        }

        Then("at least one market combo file is present") {
            assert(comboFiles.isNotEmpty()) {
                "No market combo files found under markets/. Expected at least one " +
                    "{market}/{market}-{env}.properties."
            }
        }

        Then("every market directory covers the same set of envs") {
            val marketEnvs = marketsDir.listFiles().orEmpty()
                .filter { it.isDirectory }
                .associate { dir ->
                    dir.name to dir.listFiles().orEmpty()
                        .filter { it.isFile && it.extension == "properties" }
                        .mapNotNull {
                            Regex("""^[a-z]+-([a-z]+)\.properties$""")
                                .matchEntire(it.name)?.groupValues?.get(1)
                        }
                        .toSet()
                }

            val unionEnvs = marketEnvs.values.flatten().toSortedSet()
            val violations = marketEnvs
                .mapValues { (_, envs) -> unionEnvs - envs }
                .filterValues { it.isNotEmpty() }
                .toSortedMap()

            assert(violations.isEmpty()) {
                val detail = violations.entries.joinToString("\n") { (market, missing) ->
                    "  markets/$market/: missing ${missing.sorted().joinToString { "$market-$it.properties" }}"
                }
                "Every market must cover the union of envs other markets define " +
                    "(currently: ${unionEnvs.joinToString()}):\n$detail"
            }
        }
    }
})
