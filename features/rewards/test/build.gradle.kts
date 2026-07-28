plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.rewards.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:rewards:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
