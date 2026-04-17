plugins { id("mockdonalds.kmp.presentation") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.recents.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:recents:api:domain"))
            implementation(project(":features:recents:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:recents:test"))
        }
    }
}