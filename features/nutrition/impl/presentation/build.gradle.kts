plugins { id("sampleplatter.kmp.presentation") }

val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        namespace = "com.jkjamies.sampleplatter.features.nutrition.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:nutrition:api:domain"))
            implementation(project(":features:nutrition:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        androidMain.dependencies {
            implementation(catalog.findLibrary("androidx-activity-compose").get())
        }
        commonTest.dependencies {
            implementation(project(":features:nutrition:test"))
        }
    }
}
