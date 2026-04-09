plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.login.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:login:api:domain"))
        }
        commonTest.dependencies {
        }
    }
}
