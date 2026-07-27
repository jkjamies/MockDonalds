import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
// Imported rather than written inline as `java.time.Duration`: inside a Gradle Kotlin DSL
// script `java` resolves to the JavaPluginExtension accessor, not the package, so the
// fully-qualified form fails to compile with "Unresolved reference 'time'".
import java.time.Duration

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.devtools.ksp")
    id("io.kotest")
    id("mockdonalds.detekt")
}

kotlin {
    android {
        compileSdk = 36
        minSdk = 26

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }

        withHostTest {
            isReturnDefaultValues = true
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(catalog.findLibrary("kotest-framework-engine").get())
                implementation(catalog.findLibrary("kotest-assertions-core").get())
                implementation(catalog.findLibrary("kotlinx-coroutines-test").get())
                implementation(catalog.findLibrary("turbine").get())
            }
        }
    }

    targets.configureEach {
        if (platformType == KotlinPlatformType.androidJvm) {
            compilations.configureEach {
                compileTaskProvider.configure {
                    compilerOptions {
                        freeCompilerArgs.addAll(
                            "-P",
                            "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=" +
                                "com.mockdonalds.app.core.circuit.Parcelize",
                        )
                    }
                }
            }
        }
    }
}

// Add core:test-fixtures to all modules (except itself) for shared test utilities
if (project.path != ":core:test-fixtures") {
    kotlin.sourceSets.getByName("commonTest") {
        dependencies {
            implementation(project(":core:test-fixtures"))
        }
    }
}

// Auto-generate a Kotest ProjectConfig into each module's commonTest source set.
// Kotest's KSP-based discovery on native only scans source within the module being compiled — not
// compiled classes from dependencies. This follows Kotest's recommended "Sharing Config Across
// Modules" pattern (https://kotest.io/docs/next/framework/project-config.html#sharing-config-across-modules)
// by auto-generating the per-module subclass so developers never write it themselves.
// The base config (KotestProjectConfig) lives in :core:test-fixtures. To change Kotest project-level
// settings, edit KotestProjectConfig there — all modules pick up changes automatically.
val generateKotestConfig = tasks.register("generateKotestProjectConfig") {
    val outputDir = project.layout.buildDirectory.dir("generated/kotest/commonTest/kotlin")
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile.resolve("io/kotest/provided")
        dir.mkdirs()
        dir.resolve("ProjectConfig.kt").writeText(
            """
            |package io.kotest.provided
            |
            |import com.mockdonalds.app.core.test.KotestProjectConfig
            |
            |class ProjectConfig : KotestProjectConfig()
            """.trimMargin()
        )
    }
}

kotlin.sourceSets.getByName("commonTest") {
    kotlin.srcDir(generateKotestConfig.map { project.layout.buildDirectory.dir("generated/kotest/commonTest/kotlin").get() })
}

kotlin.sourceSets.getByName("androidHostTest") {
    val catalog = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies {
        implementation(catalog.findLibrary("kotest-runner-junit6").get())
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // A stuck test otherwise hangs until the CI job's own timeout kills it — one test task ran
    // for 78 minutes and took the whole Android job down, reporting nothing useful. Kotest's
    // project config sets no timeout, so enforce one here. Per-module host suites finish in
    // seconds, so ten minutes only fires on something genuinely wrong — and names the task.
    timeout.set(Duration.ofMinutes(10))
    systemProperty("kotest.framework.config.fqn", "io.kotest.provided.ProjectConfig")
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
