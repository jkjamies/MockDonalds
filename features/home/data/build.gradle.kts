plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.home.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:domain"))
            implementation(project(":core:network"))
        }
    }
}
