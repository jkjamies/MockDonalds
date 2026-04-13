plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.analytics.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:analytics:api"))
        }
    }
}
