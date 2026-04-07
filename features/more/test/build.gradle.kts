plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.more.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:more:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
