plugins {
    id("sampleplatter.kmp.data")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.scan.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:scan:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
