plugins {
    id("mockdonalds.kmp.library")
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.order.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.circuit.runtime)
            api(project(":core:common"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
