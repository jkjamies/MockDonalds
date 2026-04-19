plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.buildconfig.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:build-config:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
