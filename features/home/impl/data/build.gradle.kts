plugins {
    id("sampleplatter.kmp.data")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.home.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
