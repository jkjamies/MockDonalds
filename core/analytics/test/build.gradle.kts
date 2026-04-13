plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.analytics.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:analytics:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
