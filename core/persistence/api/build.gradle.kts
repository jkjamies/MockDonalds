plugins {
    id("sampleplatter.kmp.library")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.persistence.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.sqldelight.runtime)
            api(libs.sqldelight.coroutines.extensions)
        }
    }
}
