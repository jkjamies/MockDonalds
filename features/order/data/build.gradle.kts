plugins {
    id("mockdonalds.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.order.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:order:api"))
            implementation(project(":core:common"))
            implementation(project(":core:network"))
        }
    }
}
