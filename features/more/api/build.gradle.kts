plugins {
    id("mockdonalds.kmp.library")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.more.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.circuit.runtime)
            api(project(":core:common"))
            api(project(":core:centerpost"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
