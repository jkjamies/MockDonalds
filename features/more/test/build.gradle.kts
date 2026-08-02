plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.more.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:more:api:domain"))
            api(project(":features:more:api:navigation"))
            api(project(":core:test-fixtures"))
            api(project(":core:build-config:api"))
            api(project(":core:build-config:test"))
        }
    }
}
