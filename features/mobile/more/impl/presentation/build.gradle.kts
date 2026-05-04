plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.more.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:more:api:domain"))
            implementation(project(":features:mobile:more:api:navigation"))
            implementation(project(":features:mobile:profile:api:navigation"))
            implementation(project(":features:mobile:recents:api:navigation"))
            implementation(project(":features:mobile:nutrition:api:navigation"))
            implementation(project(":core:build-config:api"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:more:test"))
        }
    }
}
