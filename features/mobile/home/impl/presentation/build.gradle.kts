plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.home.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:home:api:domain"))
            implementation(project(":features:mobile:home:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:home:test"))
        }
    }
}
