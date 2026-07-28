import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.jkjamies.sampleplatter.buildlogic.BuildVariantResolver
import java.util.Properties

plugins {
    id("sampleplatter.kmp.data")
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.remoteconfig.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:remote-config:api"))
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.harness.ff.android)
        }
    }
}

val env: String = BuildVariantResolver.env(project)

logger.lifecycle("core:remote-config:impl → env=$env")

fun loadProps(path: String): Map<String, String> {
    val f = file(path)
    require(f.exists()) { "Missing remote-config config file: $path" }
    val props = Properties().apply { f.inputStream().use { load(it) } }
    return props.entries.associate { (k, v) -> k.toString() to v.toString() }
}

val configPath = "config/$env.properties"
require(file(configPath).exists()) {
    "Unknown env: $env (expected $configPath)"
}
val config = loadProps(configPath)

buildkonfig {
    packageName = "com.jkjamies.sampleplatter.core.remoteconfig"
    objectName = "RemoteConfigBuildConfig"
    exposeObjectWithName = "RemoteConfigBuildConfig"
    defaultConfigs {
        config.forEach { (key, value) ->
            buildConfigField(STRING, key, value)
        }
    }
}
