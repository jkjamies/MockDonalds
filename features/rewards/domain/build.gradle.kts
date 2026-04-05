plugins {
    id("mockdonalds.kmp.library")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.rewards.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:rewards:api"))
            implementation(project(":core:common"))
        }
    }
}
