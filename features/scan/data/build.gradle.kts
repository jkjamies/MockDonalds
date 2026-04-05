plugins {
    id("mockdonalds.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.scan.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:scan:api"))
            implementation(project(":core:common"))
            implementation(project(":core:network"))
        }
    }
}
