plugins { id("mockdonalds.kmp.domain") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.nutrition.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:nutrition:api:domain"))
        }
    }
}
