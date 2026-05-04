plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.shared.menu.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:shared:menu:api:domain"))
        }
    }
}
