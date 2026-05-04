plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.more.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:more:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
