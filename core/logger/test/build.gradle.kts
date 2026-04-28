plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.logger.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:logger:api"))
        }
    }
}
