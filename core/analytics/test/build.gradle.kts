plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.analytics.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:analytics:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
