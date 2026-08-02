plugins {
    id("sampleplatter.kmp.data")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.debugmenu.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:debug-menu:api:domain"))
            implementation(project(":features:debug-menu:impl:domain"))
        }
    }
}
