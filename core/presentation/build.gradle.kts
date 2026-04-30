plugins {
    id("mockdonalds.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:centerpost"))
            api(project(":core:remote-config:api"))
            implementation(compose.runtime)
        }
        androidMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(catalog.findLibrary("androidx-browser").get())
        }
    }
}
