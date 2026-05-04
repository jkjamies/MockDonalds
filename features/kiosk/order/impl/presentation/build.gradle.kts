plugins { id("mockdonalds.kmp.presentation") }

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.kiosk.order.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:kiosk:order:api:domain"))
            implementation(project(":features:kiosk:order:api:navigation"))
            implementation(project(":features:kiosk:attract:api:navigation"))
            implementation(project(":features:shared:menu:api:domain"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:kiosk:order:test"))
        }
    }
}
