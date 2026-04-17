plugins { id("mockdonalds.kmp.library") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.recents.api.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}