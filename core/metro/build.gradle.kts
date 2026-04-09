plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.metro"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:circuit"))
            api(project(":core:auth:api"))
        }
    }
}
