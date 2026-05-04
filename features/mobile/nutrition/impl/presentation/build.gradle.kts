plugins { id("mockdonalds.kmp.presentation") }

val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        namespace = "com.mockdonalds.app.features.mobile.nutrition.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:mobile:nutrition:api:domain"))
            implementation(project(":features:mobile:nutrition:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        androidMain.dependencies {
            implementation(catalog.findLibrary("androidx-activity-compose").get())
        }
        commonTest.dependencies {
            implementation(project(":features:mobile:nutrition:test"))
        }
    }
}
