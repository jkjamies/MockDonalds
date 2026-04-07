plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.scan.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:scan:api"))
            implementation(project(":core:common"))
        }
    }
}
