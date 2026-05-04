plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.login.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:mobile:login:api:domain"))
        }
        commonTest.dependencies {
        }
    }
}
