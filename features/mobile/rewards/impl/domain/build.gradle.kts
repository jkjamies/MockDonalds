plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.rewards.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:rewards:api:domain"))
        }
    }
}
