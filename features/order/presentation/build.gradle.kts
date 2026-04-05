plugins {
    id("mockdonalds.kmp.compose")
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.order.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:order:api"))
            implementation(project(":features:order:domain"))
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
