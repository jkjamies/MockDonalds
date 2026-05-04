plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.shared.menu.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:shared:menu:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
