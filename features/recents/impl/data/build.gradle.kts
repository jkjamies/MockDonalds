plugins { id("mockdonalds.kmp.data") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.recents.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:recents:api:domain"))
            implementation(project(":features:recents:impl:domain"))
            implementation(project(":core:network:api"))
            implementation(project(":core:build-config"))
        }
    }
}