plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.rewards.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:rewards:api"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
    }
}
