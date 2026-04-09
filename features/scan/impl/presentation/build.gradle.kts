plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.scan.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:scan:api:domain"))
            implementation(project(":features:scan:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:scan:test"))
        }
    }
}
