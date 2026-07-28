plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.login.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":features:login:api:domain"))
        }
        commonTest.dependencies {
        }
    }
}
