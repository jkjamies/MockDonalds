plugins {
    id("sampleplatter.kmp.data")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.login.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:login:impl:domain"))
            implementation(project(":core:network:api"))
        }
    }
}
