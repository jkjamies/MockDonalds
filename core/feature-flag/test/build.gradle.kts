plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.featureflag.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:feature-flag:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
