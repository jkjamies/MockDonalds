plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.rewards.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:rewards:api:domain"))
            implementation(project(":features:mobile:rewards:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:rewards:test"))
        }
    }
}
