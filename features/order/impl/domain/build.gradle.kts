plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.order.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:order:api:domain"))
        }
    }
}
