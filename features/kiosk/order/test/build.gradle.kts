plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.order.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:kiosk:order:api:domain"))
            api(project(":features:order:api:domain"))
            api(project(":features:order:test"))
            api(project(":core:test-fixtures"))
        }
    }
}
