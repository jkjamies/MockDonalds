plugins { id("sampleplatter.kmp.domain") }

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.recents.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:recents:api:domain"))
        }
    }
}