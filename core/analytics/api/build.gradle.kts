plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.analytics.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
        }
    }
}
