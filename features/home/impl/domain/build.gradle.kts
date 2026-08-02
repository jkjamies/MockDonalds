plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.home.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:home:api:domain"))
        }
        commonTest.dependencies {
        }
    }
}
