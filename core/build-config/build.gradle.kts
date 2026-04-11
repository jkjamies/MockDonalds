import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    id("mockdonalds.kmp.domain")
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.buildconfig"
    }
}

val market: String = providers.gradleProperty("market").getOrElse("us")
val env: String = providers.gradleProperty("env").getOrElse("dev")

logger.lifecycle("core:build-config → market=$market env=$env")

fun loadProps(path: String): Map<String, String> {
    val f = file(path)
    require(f.exists()) { "Missing build-config file: $path" }
    val props = Properties().apply { f.inputStream().use { load(it) } }
    return props.entries.associate { (k, v) -> k.toString() to v.toString() }
}

val defaults = loadProps("Defaults.properties")
val comboPath = "markets/$market-$env.properties"
require(file(comboPath).exists()) {
    "Unknown market/env combo: $market-$env (expected $comboPath)"
}
val combo = loadProps(comboPath)

val merged: Map<String, String> = defaults + combo

buildkonfig {
    packageName = "com.mockdonalds.app.core.buildconfig"
    objectName = "BuildConfig"
    defaultConfigs {
        merged.forEach { (key, value) ->
            buildConfigField(STRING, key, value)
        }
    }
}
