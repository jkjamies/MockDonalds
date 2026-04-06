plugins {
    id("mockdonalds.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.zacsweers.metro")
    id("com.google.devtools.ksp")
}

val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

ksp {
    arg("circuit.codegen.mode", "metro")
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
                implementation(catalog.findLibrary("circuit-runtime-ui").get())
            }
        }
    }
}

val circuitCodegen = catalog.findLibrary("circuit-codegen").get()

// Per-target KSP: each target processes its own sources (commonMain + targetMain)
// Android sees presenters (commonMain) + UIs (androidMain) → generates both factory types
// iOS targets see presenters (commonMain) only → generates presenter factories only
dependencies {
    add("kspAndroid", circuitCodegen)
    add("kspIosX64", circuitCodegen)
    add("kspIosArm64", circuitCodegen)
    add("kspIosSimulatorArm64", circuitCodegen)
}
