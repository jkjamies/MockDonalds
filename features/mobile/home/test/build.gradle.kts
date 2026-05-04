plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.home.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:home:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
