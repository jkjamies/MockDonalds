plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.recents.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:recents:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}