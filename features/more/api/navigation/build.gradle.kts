plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.more.api.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.circuit.runtime)
            api(project(":core:common"))
        }
    }
}
