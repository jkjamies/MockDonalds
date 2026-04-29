plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.remoteconfig.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:remote-config:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
