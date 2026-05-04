plugins { id("mockdonalds.kmp.library") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.identify.api.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:circuit"))
        }
    }
}
