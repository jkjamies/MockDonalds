plugins {
    id("sampleplatter.kmp.presentation")
}


kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.order.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:order:api:domain"))
            implementation(project(":features:order:api:navigation"))
            implementation(project(":core:strata"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:order:test"))
        }
    }
}
