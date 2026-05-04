plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.home.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:home:api:domain"))
        }
        commonTest.dependencies {
        }
    }
}
