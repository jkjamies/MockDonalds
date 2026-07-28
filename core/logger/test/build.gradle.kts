plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.logger.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:logger:api"))
        }
    }
}
