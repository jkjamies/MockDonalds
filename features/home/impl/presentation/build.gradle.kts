plugins {
    id("sampleplatter.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.home.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:home:api:domain"))
            implementation(project(":features:home:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:home:test"))
        }
    }
}
