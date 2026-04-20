import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.mockdonalds.buildlogic.BuildVariantResolver
import java.util.Properties

plugins {
    id("mockdonalds.kmp.domain")
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.featureflag.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:feature-flag:api"))
        }
        androidMain.dependencies {
            implementation(libs.harness.ff.android)
        }
    }
}

val env: String = BuildVariantResolver.env(project)

logger.lifecycle("core:feature-flag:impl → env=$env")

fun loadProps(path: String): Map<String, String> {
    val f = file(path)
    require(f.exists()) { "Missing feature-flag config file: $path" }
    val props = Properties().apply { f.inputStream().use { load(it) } }
    return props.entries.associate { (k, v) -> k.toString() to v.toString() }
}

val configPath = "config/$env.properties"
require(file(configPath).exists()) {
    "Unknown env: $env (expected $configPath)"
}
val config = loadProps(configPath)

buildkonfig {
    packageName = "com.mockdonalds.app.core.featureflag"
    objectName = "FeatureFlagBuildConfig"
    exposeObjectWithName = "FeatureFlagBuildConfig"
    defaultConfigs {
        config.forEach { (key, value) ->
            buildConfigField(STRING, key, value)
        }
    }
}
