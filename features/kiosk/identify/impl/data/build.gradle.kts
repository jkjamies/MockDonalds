plugins { id("mockdonalds.kmp.data") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.identify.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:kiosk:identify:api:domain"))
            implementation(project(":features:kiosk:identify:impl:domain"))
            implementation(project(":core:network:api"))
            implementation(project(":core:build-config:api"))
        }
    }
}
