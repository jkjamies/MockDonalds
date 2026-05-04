plugins { id("mockdonalds.kmp.data") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.attract.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:kiosk:attract:api:domain"))
            implementation(project(":features:kiosk:attract:impl:domain"))
            implementation(project(":core:network:api"))
            implementation(project(":core:build-config:api"))
        }
    }
}
