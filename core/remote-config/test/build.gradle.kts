plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.remoteconfig.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:remote-config:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
