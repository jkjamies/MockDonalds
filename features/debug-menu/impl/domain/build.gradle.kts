plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.debugmenu.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:debug-menu:api:domain"))
        }
    }
}
