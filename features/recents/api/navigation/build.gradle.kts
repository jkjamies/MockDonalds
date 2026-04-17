plugins { id("mockdonalds.kmp.library") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.recents.api.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:circuit"))
        }
    }
}