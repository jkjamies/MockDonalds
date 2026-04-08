plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.login.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:login:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
