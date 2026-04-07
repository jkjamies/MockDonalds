plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.scan.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:scan:api"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
        commonTest.dependencies {
            implementation(project(":features:scan:test"))
        }
    }
}
