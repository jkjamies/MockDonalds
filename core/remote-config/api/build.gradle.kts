plugins {
    id("sampleplatter.kmp.domain")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.remoteconfig.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:strata"))
            api(libs.kotlinx.serialization.core)
        }
    }
}
