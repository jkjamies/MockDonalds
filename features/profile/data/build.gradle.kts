plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.profile.data"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:profile:domain"))
        }
        commonTest.dependencies {
        }
    }
}
