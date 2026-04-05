plugins {
    id("mockdonalds.kmp.library")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.scan.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:scan:api"))
            implementation(project(":core:common"))
        }
    }
}
