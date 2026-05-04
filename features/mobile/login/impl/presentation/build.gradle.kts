plugins {
    id("mockdonalds.kmp.presentation")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.login.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:login:api:domain"))
            implementation(project(":features:mobile:login:api:navigation"))
            implementation(project(":core:auth:api"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:login:test"))
        }
    }
}
