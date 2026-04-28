plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.logger.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kermit)
        }
    }
}
