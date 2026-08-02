plugins { id("sampleplatter.kmp.library") }

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.recents.api.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:strata"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}