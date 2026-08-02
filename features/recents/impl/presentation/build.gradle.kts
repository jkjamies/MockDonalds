plugins { id("sampleplatter.kmp.presentation") }

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.recents.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:recents:api:domain"))
            implementation(project(":features:recents:api:navigation"))
            implementation(project(":core:strata"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:recents:test"))
        }
    }
}