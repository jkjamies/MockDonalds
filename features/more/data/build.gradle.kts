plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.more.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:more:domain"))
            implementation(project(":core:network"))
        }
    }
}
