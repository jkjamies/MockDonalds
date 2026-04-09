plugins {
    id("mockdonalds.kmp.presentation")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.profile.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:profile:api:domain"))
            implementation(project(":features:profile:api:navigation"))
            implementation(project(":core:auth"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:profile:test"))
        }
    }
}
