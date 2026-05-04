plugins {
    id("mockdonalds.kmp.presentation")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.profile.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:profile:api:domain"))
            implementation(project(":features:mobile:profile:api:navigation"))
            implementation(project(":core:auth:api"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:profile:test"))
        }
    }
}
