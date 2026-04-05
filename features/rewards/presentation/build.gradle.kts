plugins {
    id("mockdonalds.kmp.compose")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.rewards.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:rewards:api"))
            implementation(project(":features:rewards:domain"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
    }
}
