plugins {
    id("mockdonalds.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.rewards.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:rewards:api"))
            implementation(project(":core:common"))
            implementation(project(":core:network"))
        }
    }
}
