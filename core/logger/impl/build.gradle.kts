plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.logger.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:logger:api"))
            implementation(project(":core:build-config:api"))
        }
    }
}
