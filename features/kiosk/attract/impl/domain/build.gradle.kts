plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.attract.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:kiosk:attract:api:domain"))
        }
    }
}
