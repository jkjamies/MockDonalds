plugins {
    id("sampleplatter.kmp.library")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.scan.api.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.circuit.runtime)
            api(project(":core:circuit"))
        }
    }
}
