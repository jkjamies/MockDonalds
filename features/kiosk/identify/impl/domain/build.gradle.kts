plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.identify.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:kiosk:identify:api:domain"))
            implementation(project(":core:auth:api"))
        }
    }
}
