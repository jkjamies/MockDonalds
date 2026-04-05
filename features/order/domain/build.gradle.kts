plugins {
    id("mockdonalds.kmp.library")
}


kotlin {
    androidLibrary {
        namespace = "com.mockdonalds.app.features.order.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:order:api"))
            implementation(project(":core:common"))
        }
    }
}
