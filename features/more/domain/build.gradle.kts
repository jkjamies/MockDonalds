plugins {
    id("mockdonalds.kmp.library")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.more.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:more:api"))
            implementation(project(":core:common"))
        }
    }
}
