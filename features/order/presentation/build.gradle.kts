plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.order.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:order:api"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
        commonTest.dependencies {
            implementation(project(":features:order:test"))
        }
    }
}
