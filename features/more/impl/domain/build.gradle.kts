plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.more.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:more:api:domain"))
        }
    }
}
