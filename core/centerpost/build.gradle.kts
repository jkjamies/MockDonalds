plugins {
    id("mockdonalds.kmp.domain")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.centerpost"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(compose.runtime)
        }
    }
}
