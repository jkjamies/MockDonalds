plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.rewards.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:rewards:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
