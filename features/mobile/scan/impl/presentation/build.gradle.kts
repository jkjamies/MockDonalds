plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.scan.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:scan:api:domain"))
            implementation(project(":features:mobile:scan:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:scan:test"))
        }
    }
}
