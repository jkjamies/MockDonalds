plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.order.api.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
