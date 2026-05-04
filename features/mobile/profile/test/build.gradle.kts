plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.profile.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:profile:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
