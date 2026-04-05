plugins {
    id("mockdonalds.kmp.compose")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.scan.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:scan:api"))
            implementation(project(":features:scan:domain"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
    }
}
