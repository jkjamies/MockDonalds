plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.login.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:login:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
