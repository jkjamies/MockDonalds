plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.scan.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:scan:api:domain"))
        }
    }
}
