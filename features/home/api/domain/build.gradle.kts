plugins {
    id("sampleplatter.kmp.library")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.home.api.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:strata"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
