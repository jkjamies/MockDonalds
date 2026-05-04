plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.debugmenu.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:debug-menu:api:domain"))
            implementation(project(":features:mobile:debug-menu:impl:domain"))
        }
    }
}
