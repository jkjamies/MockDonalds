plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.recents.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:recents:api:domain"))
        }
    }
}