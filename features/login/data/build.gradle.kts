plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.login.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:login:domain"))
            implementation(project(":core:common"))
            implementation(project(":core:network"))
        }
    }
}
