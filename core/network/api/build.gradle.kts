plugins {
    id("sampleplatter.kmp.library")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.core.network.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
        }
    }
}
