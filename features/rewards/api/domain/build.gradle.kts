plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.rewards.api.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:centerpost"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
