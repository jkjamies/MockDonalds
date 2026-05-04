plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.recents.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:recents:api:domain"))
        }
    }
}