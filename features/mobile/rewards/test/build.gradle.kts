plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.rewards.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:rewards:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
