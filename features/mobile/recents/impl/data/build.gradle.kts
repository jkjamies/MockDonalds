plugins { id("mockdonalds.kmp.data") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.recents.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:recents:api:domain"))
            implementation(project(":features:mobile:recents:impl:domain"))
            implementation(project(":core:network:api"))
            implementation(project(":core:build-config:api"))
        }
    }
}