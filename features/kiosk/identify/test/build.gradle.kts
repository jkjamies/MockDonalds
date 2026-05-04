plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.identify.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:kiosk:identify:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
