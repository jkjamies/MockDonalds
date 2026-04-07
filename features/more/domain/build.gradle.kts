plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.more.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:more:api"))
            implementation(project(":core:common"))
        }
    }
}
