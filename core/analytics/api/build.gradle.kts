plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.analytics.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
        }
    }
}
