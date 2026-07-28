plugins {
    id("sampleplatter.kmp.data")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.rewards.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:rewards:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
