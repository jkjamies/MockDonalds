plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.debugmenu.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:debug-menu:api:domain"))
        }
    }
}
