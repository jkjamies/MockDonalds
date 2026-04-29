plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.debugmenu.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:debug-menu:api:domain"))
            api(project(":features:debug-menu:api:navigation"))
            api(project(":core:test-fixtures"))
            api(project(":core:remote-config:api"))
            api(project(":core:remote-config:test"))
            api(project(":core:build-config:api"))
            api(project(":core:build-config:test"))
        }
    }
}
