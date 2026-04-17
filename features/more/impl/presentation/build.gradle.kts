plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.more.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:more:api:domain"))
            implementation(project(":features:more:api:navigation"))
            implementation(project(":features:profile:api:navigation"))
            implementation(project(":features:recents:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:more:test"))
        }
    }
}
