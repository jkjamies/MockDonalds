plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.profile.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:profile:api:domain"))
        }
        commonTest.dependencies {
        }
    }
}
