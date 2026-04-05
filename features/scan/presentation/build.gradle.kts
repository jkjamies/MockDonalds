plugins {
    id("mockdonalds.kmp.compose")
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.scan.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:scan:api"))
            implementation(project(":features:scan:domain"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
            implementation(libs.circuit.foundation)
            implementation(libs.circuit.runtime.presenter)
            implementation(libs.circuit.runtime.ui)
            implementation(libs.circuit.retained)
            implementation(libs.circuit.codegen.annotations)
        }
    }
}
