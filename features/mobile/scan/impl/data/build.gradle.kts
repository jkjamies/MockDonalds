plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.scan.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:scan:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
