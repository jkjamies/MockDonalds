plugins {
    id("mockdonalds.kmp.data")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.shared.menu.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:shared:menu:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
