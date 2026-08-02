plugins {
    id("sampleplatter.kmp.data")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.more.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:more:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
