plugins {
    id("mockdonalds.kmp.presentation")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.debugmenu.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:debug-menu:api:domain"))
            implementation(project(":features:mobile:debug-menu:api:navigation"))
            implementation(project(":features:mobile:more:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            api(project(":core:remote-config:api"))
            api(project(":core:build-config:api"))
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:debug-menu:test"))
        }
        getByName("androidDeviceTest").dependencies {
            implementation(project(":core:build-config:test"))
        }
    }
}
