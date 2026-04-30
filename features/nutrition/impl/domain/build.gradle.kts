plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.nutrition.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:nutrition:api:domain"))
        }
    }
}
