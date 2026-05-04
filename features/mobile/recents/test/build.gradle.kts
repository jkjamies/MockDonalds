plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.recents.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:recents:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}