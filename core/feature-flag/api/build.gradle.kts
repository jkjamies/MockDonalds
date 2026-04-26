plugins {
    id("mockdonalds.kmp.domain")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.featureflag.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
            implementation(compose.runtime)
        }
    }
}
