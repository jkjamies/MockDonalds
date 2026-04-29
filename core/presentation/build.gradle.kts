plugins {
    id("mockdonalds.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
            api(project(":core:remote-config:api"))
            implementation(compose.runtime)
        }
    }
}
