plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.profile.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:profile:api:domain"))
        }
        commonTest.dependencies {
        }
    }
}
