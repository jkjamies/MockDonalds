plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.rewards.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:rewards:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
