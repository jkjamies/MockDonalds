plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.more.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:more:api:domain"))
        }
    }
}
