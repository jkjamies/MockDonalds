plugins {
    id("mockdonalds.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.home.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:api"))
            implementation(project(":core:common"))
            implementation(project(":core:network"))
        }
    }
}
