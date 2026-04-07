plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.rewards.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:rewards:api"))
            implementation(project(":core:common"))
        }
    }
}
