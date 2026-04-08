plugins {
    id("mockdonalds.kmp.domain")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.circuit"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(libs.circuit.runtime)
            implementation(compose.runtime)
        }
    }
}
