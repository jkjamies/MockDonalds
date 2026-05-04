plugins { id("mockdonalds.kmp.data") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.order.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:kiosk:order:api:domain"))
            implementation(project(":features:kiosk:order:impl:domain"))
            implementation(project(":core:network:api"))
            implementation(project(":core:build-config:api"))
        }
    }
}
