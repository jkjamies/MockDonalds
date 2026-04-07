plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.home.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:home:api"))
            implementation(project(":core:common"))
        }
        commonTest.dependencies {
        }
    }
}
