plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.profile.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:profile:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
