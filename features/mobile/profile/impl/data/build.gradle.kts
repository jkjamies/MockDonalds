plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.profile.data"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:profile:impl:domain"))
        }
        commonTest.dependencies {
        }
    }
}
