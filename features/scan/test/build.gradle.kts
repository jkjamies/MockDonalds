plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.scan.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:scan:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
