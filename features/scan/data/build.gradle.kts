plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.scan.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:scan:domain"))
            implementation(project(":core:network"))
        }
    }
}
