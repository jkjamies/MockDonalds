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
        androidMain {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(catalog.findLibrary("coil-compose").get())
            }
        }
    }
}
