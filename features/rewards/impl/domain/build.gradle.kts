plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.rewards.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:rewards:api:domain"))
        }
    }
}
