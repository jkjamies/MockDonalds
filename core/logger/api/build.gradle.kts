plugins {
    id("sampleplatter.kmp.library")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.logger.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kermit)
        }
    }
}
