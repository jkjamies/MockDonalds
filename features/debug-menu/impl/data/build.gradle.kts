plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.debugmenu.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:debug-menu:api:domain"))
            implementation(project(":features:debug-menu:impl:domain"))
        }
    }
}
