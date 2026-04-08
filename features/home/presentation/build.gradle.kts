plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.home.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:api:domain"))
            implementation(project(":features:home:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
        commonTest.dependencies {
            implementation(project(":features:home:test"))
        }
    }
}
