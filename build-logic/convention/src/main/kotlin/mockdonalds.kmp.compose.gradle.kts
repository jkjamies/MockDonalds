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
            // Make KSP-generated factories visible to all targets
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                
                implementation(catalog.findLibrary("coil-compose").get())
                implementation(catalog.findLibrary("circuit-foundation").get())
                implementation(catalog.findLibrary("circuit-runtime-presenter").get())
                implementation(catalog.findLibrary("circuit-runtime-ui").get())
                implementation(catalog.findLibrary("circuit-retained").get())
                implementation(catalog.findLibrary("circuit-codegen-annotations").get())
            }
        }
    }
}

val circuitCodegen = catalog.findLibrary("circuit-codegen").get()

dependencies {
    add("kspCommonMainMetadata", circuitCodegen)
}

// Ensure all compilations depend on the common KSP task
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
