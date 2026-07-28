plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.order.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:order:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
