plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.scan.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:scan:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
