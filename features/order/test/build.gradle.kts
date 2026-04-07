plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.order.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:order:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
