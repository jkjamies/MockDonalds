plugins {
    id("mockdonalds.kmp.library")
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.splash.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:splash:api"))
            implementation(project(":core:common"))
        }
    }
}
