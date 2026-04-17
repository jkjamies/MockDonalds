plugins {
    id("mockdonalds.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.zacsweers.metro")
}

val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

metro {
    enableCircuitCodegen.set(true)
}

kotlin {
    android {
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            packaging {
                resources.excludes.addAll(
                    listOf(
                        "META-INF/AL2.0",
                        "META-INF/LGPL2.1",
                        "META-INF/LICENSE.md",
                        "META-INF/LICENSE-notice.md",
                    )
                )
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)

                implementation(catalog.findLibrary("circuit-foundation").get())
                api(catalog.findLibrary("circuit-runtime-presenter").get())
                implementation(catalog.findLibrary("circuit-retained").get())
                implementation(catalog.findLibrary("circuit-codegen-annotations").get())
            }
        }
        commonTest {
            dependencies {
                implementation(catalog.findLibrary("circuit-test").get())
            }
        }
        androidMain {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(catalog.findLibrary("coil-compose").get())
            }
        }
    }
}

kotlin.sourceSets.getByName("androidDeviceTest") {
    dependencies {
        implementation(catalog.findLibrary("compose-ui-test-junit4").get())
        implementation(project(":core:test-fixtures"))
    }
}

// Compose Multiplatform 1.10.3 doesn't configure outputDirectory for androidDeviceTest resource copy task
tasks.matching { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }.configureEach {
    enabled = false
}
