plugins {
    id("mockdonalds.kmp.domain")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.remoteconfig.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
            api(libs.kotlinx.serialization.core)
        }
    }
}
