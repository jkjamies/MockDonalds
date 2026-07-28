plugins {
    id("sampleplatter.kmp.data")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.profile.data"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:profile:impl:domain"))
        }
        commonTest.dependencies {
        }
    }
}
