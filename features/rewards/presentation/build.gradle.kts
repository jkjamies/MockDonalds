plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.rewards.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:rewards:api:domain"))
            implementation(project(":features:rewards:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:rewards:test"))
        }
    }
}
