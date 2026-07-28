plugins { id("sampleplatter.kmp.domain") }

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.nutrition.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:nutrition:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
