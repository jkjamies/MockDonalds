plugins { id("sampleplatter.kmp.library") }

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.nutrition.api.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:circuit"))
        }
    }
}
