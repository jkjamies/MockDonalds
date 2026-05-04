plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.nutrition.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:nutrition:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
