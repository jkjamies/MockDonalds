plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.scan.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:scan:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
