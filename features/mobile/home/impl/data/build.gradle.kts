plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.home.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:home:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
