plugins {
    id("sampleplatter.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.more.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:more:api:domain"))
            implementation(project(":features:more:api:navigation"))
            implementation(project(":features:profile:api:navigation"))
            implementation(project(":features:recents:api:navigation"))
            implementation(project(":features:nutrition:api:navigation"))
            implementation(project(":core:build-config:api"))
            implementation(project(":core:strata"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:more:test"))
        }
    }
}
