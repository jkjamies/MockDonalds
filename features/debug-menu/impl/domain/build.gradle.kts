plugins {
    id("sampleplatter.kmp.domain")
}

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.debugmenu.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:debug-menu:api:domain"))
        }
    }
}
