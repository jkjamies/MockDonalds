plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.home.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:home:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
