plugins { id("mockdonalds.kmp.presentation") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.identify.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:kiosk:identify:api:domain"))
            implementation(project(":features:kiosk:identify:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:kiosk:identify:test"))
        }
    }
}
