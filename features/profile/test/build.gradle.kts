plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.profile.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:profile:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
