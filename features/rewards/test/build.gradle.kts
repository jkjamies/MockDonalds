plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.rewards.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:rewards:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
