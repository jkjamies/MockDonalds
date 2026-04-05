plugins {
    id("mockdonalds.kmp.compose")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.more.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:more:api"))
            implementation(project(":features:more:domain"))
            implementation(project(":core:theme"))
            implementation(project(":core:common"))
        }
    }
}
