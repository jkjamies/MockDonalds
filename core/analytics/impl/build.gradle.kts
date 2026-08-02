plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.analytics.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:analytics:api"))
        }
    }
}
