plugins {
    id("sampleplatter.kmp.presentation")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.login.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:login:api:domain"))
            implementation(project(":features:login:api:navigation"))
            implementation(project(":core:auth:api"))
            implementation(project(":core:strata"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:login:test"))
        }
    }
}
