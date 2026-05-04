plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.scan.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:scan:api:domain"))
        }
    }
}
