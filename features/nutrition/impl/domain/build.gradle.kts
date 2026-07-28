plugins { id("sampleplatter.kmp.domain") }

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.nutrition.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:nutrition:api:domain"))
        }
    }
}
