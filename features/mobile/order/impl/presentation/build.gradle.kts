plugins {
    id("mockdonalds.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.order.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:shared:menu:api:domain"))
            implementation(project(":features:mobile:order:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:shared:menu:test"))
        }
    }
}
