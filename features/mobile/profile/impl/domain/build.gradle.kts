plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.profile.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:profile:api:domain"))
        }
        commonTest.dependencies {
        }
    }
}
