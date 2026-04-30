plugins { id("mockdonalds.kmp.data") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.nutrition.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:nutrition:api:domain"))
            implementation(project(":features:nutrition:impl:domain"))
            implementation(project(":core:build-config:api"))
        }
        commonTest.dependencies {
            implementation(project(":core:build-config:test"))
        }
    }
}
