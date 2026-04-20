plugins {
    id("mockdonalds.kmp.presentation")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.debugmenu.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:debug-menu:api:domain"))
            implementation(project(":features:debug-menu:api:navigation"))
            implementation(project(":features:more:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:debug-menu:test"))
        }
    }
}
