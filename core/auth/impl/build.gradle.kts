plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.auth.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:auth:api"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
