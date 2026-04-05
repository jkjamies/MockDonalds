plugins {
    id("mockdonalds.kmp.compose")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.home.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:api"))
            implementation(project(":features:home:domain"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
    }
}
