plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:auth:api"))
            api(project(":core:centerpost"))
            api(project(":core:network:api"))
            api(libs.kotlinx.coroutines.test)
            api(libs.kotest.framework.engine)
            api(libs.kotest.assertions.core)
            api(libs.turbine)
        }
    }
}
