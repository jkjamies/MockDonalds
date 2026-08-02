plugins { id("sampleplatter.kmp.domain") }

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.recents.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:recents:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}