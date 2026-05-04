plugins { id("mockdonalds.kmp.library") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.order.api.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
            api(project(":features:order:api:domain"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
