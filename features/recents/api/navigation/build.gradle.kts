plugins { id("sampleplatter.kmp.library") }

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.recents.api.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:circuit"))
        }
    }
}