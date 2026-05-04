plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.login.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:login:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
