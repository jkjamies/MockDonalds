plugins { id("mockdonalds.kmp.presentation") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.recents.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:recents:api:domain"))
            implementation(project(":features:mobile:recents:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:recents:test"))
        }
    }
}