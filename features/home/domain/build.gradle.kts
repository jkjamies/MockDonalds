plugins {
    id("mockdonalds.kmp.library")
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.home.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:api"))
            implementation(project(":core:common"))
        }
    }
}
