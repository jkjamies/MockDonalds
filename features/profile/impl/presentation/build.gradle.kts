plugins {
    id("sampleplatter.kmp.presentation")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.profile.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:profile:api:domain"))
            implementation(project(":features:profile:api:navigation"))
            implementation(project(":core:auth:api"))
            implementation(project(":core:strata"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:profile:test"))
        }
    }
}
