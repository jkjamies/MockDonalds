plugins {
    id("sampleplatter.kmp.library")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.profile.api.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:strata"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
