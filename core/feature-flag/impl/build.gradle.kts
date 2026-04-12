plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.featureflag.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:feature-flag:api"))
        }
    }
}
