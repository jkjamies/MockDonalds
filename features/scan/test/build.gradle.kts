plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.scan.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:scan:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
