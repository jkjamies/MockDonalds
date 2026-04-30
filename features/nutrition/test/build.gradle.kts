plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.nutrition.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:nutrition:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
