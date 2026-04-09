plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.auth.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:auth:api"))
        }
    }
}
