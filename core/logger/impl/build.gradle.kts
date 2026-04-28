plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.logger.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:logger:api"))
            implementation(project(":core:build-config:api"))
        }
    }
}
