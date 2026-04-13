plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.network.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
        }
    }
}
