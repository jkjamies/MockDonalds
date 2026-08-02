plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.home.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:home:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
