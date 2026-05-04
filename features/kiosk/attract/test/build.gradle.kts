plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.attract.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:kiosk:attract:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
