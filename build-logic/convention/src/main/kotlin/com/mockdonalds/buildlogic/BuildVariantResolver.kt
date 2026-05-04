package com.mockdonalds.buildlogic

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

// Single source of truth for resolving the (market, env, buildType) tuple at configure time.
//
// Resolution chain (same on Android + iOS; each Gradle invocation only sees one platform's signals):
//   1. Explicit `-Pmarket` / `-Penv` / `-PbuildType` — used by iOS's preBuildScript (forwarding
//      xcconfig values MARKET/ENV/KOTLIN_FRAMEWORK_BUILD_TYPE) and by CI or ad-hoc CLI invocations.
//   2. AGP variant task-name parsing — Android Studio's Build Variants window emits task names like
//      `assembleUsIntDebug` / `compileUsIntDebugKotlin` / `connectedUsIntDebugAndroidTest`. Parsing
//      the first such camelCase triple lets AS's picker drive market/env/buildType automatically.
//   3. Defaults — `us` / `int` / `debug` (with `release` inferred from variant-agnostic `*Release*`
//      task names as a last-resort fallback for buildType).
//
// When adding a new market or env, update `variantRe` below — this is the only place the alternation
// lives. `core:build-config:impl` and `core:remote-config:impl` both read through this object.
object BuildVariantResolver {
    private val variantRe = Regex("""(?i)(us|ca|de|au|core)(Int|Mte|Prod)(Debug|Release)""")
    private val releaseTaskRe = Regex("""[a-z]Release(?=[A-Z]|$)""")

    data class Variant(val market: String?, val env: String?, val buildType: String?)

    private fun parse(gradle: Gradle): Variant = gradle.startParameter.taskNames
        .firstNotNullOfOrNull { variantRe.find(it) }
        ?.destructured
        ?.let { (m, e, b) -> Variant(m.lowercase(), e.lowercase(), b.lowercase()) }
        ?: Variant(null, null, null)

    fun market(project: Project): String =
        project.providers.gradleProperty("market").orNull
            ?: parse(project.gradle).market
            ?: "us"

    fun env(project: Project): String =
        project.providers.gradleProperty("env").orNull
            ?: parse(project.gradle).env
            ?: "int"

    fun buildType(project: Project): String {
        val resolved = project.providers.gradleProperty("buildType").orNull
            ?: parse(project.gradle).buildType
            ?: if (project.gradle.startParameter.taskNames.any { releaseTaskRe.containsMatchIn(it) }) "release"
            else "debug"
        require(resolved == "debug" || resolved == "release") {
            "Unknown buildType: '$resolved' (expected 'debug' or 'release')"
        }
        return resolved
    }

    // Resolves which application module is driving the current Gradle invocation.
    // Detection chain:
    //   1. Explicit `-PappType=Consumer|Kiosk` — used by CI/CLI overrides.
    //   2. Task-name inspection — `:kioskApp:` or `kioskApp:` prefix in any task name → Kiosk.
    //   3. Default — Consumer.
    fun appType(project: Project): String {
        val explicit = project.providers.gradleProperty("appType").orNull
        if (explicit != null) {
            require(explicit == "Consumer" || explicit == "Kiosk") {
                "Unknown appType: '$explicit' (expected 'Consumer' or 'Kiosk')"
            }
            return explicit
        }
        val taskNames = project.gradle.startParameter.taskNames
        return if (taskNames.any { it.contains(":kioskApp:") || it.startsWith("kioskApp:") || it.contains(":kioskComposeApp:") || it.startsWith("kioskComposeApp:") }) {
            "Kiosk"
        } else {
            "Consumer"
        }
    }
}
