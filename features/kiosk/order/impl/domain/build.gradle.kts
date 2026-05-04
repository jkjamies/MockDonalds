plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.order.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:kiosk:order:api:domain"))
        }
    }
}
