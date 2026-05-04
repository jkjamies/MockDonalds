plugins { id("mockdonalds.kmp.data") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.nutrition.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:nutrition:api:domain"))
            implementation(project(":features:mobile:nutrition:impl:domain"))
            implementation(project(":core:build-config:api"))
        }
        commonTest.dependencies {
            implementation(project(":core:build-config:test"))
        }
    }
}
