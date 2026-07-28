plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.metro"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:circuit"))
            api(project(":core:analytics:api"))
            api(project(":core:auth:api"))
            api(project(":core:build-config:api"))
            api(project(":core:logger:api"))
        }
    }
}
