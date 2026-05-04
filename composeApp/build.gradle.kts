plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
    alias(libs.plugins.kmp.nativecoroutines)
    alias(libs.plugins.sqldelight)
}

metro {
    enableCircuitCodegen.set(true)
}

// SQLDelight aggregator — composeApp owns the single application-wide `AppDatabase`.
// Features contribute their own `.sq` schemas inside `features/{name}/impl/data/`
// (each applying the SQLDelight plugin and declaring the same `AppDatabase` name).
// Add `dependency(project(":features:<name>:impl:data"))` here when a feature
// starts contributing tables. Mirrors Slack/Cash App's single-DB-with-feature-owned-
// schemas pattern: composeApp is the leaf consumer that already imports every feature,
// so this preserves the "core never imports features" Konsist boundary.
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.mockdonalds.app.persistence")
        }
    }
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.composeapp"
        compileSdk = 36
        minSdk = 26

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

            // Export consumer feature modules for iOS consumption — every public-facing
            // submodule under `features/{mobile,shared}/`. Kiosk features under
            // `features/kiosk/` are NOT exported (Konsist enforces the boundary).
            listOf("mobile", "shared").forEach { grouping ->
                rootDir.resolve("features/$grouping").walkTopDown()
                    .filter { dir -> dir.isDirectory && dir.resolve("build.gradle.kts").exists() }
                    .filter { dir ->
                        val rel = dir.relativeTo(rootDir).path
                        // Only export public-facing submodules (api/* and impl/presentation).
                        // impl/data + impl/domain are wired internally; test/ never ships.
                        rel.contains("/api/") || rel.endsWith("/impl/presentation")
                    }
                    .forEach { export(project(":" + it.relativeTo(rootDir).path.replace("/", ":"))) }
            }
            export(project(":core:circuit"))
            export(project(":core:remote-config:impl"))
            export(project(":core:build-config:api"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Consumer feature modules — pick up every Gradle submodule under
            // `features/{mobile,shared}/`. The host is an app entry point with no
            // transitive consumers, so `implementation` for all is fine — the
            // api/implementation distinction would only matter for libraries that
            // re-expose their deps. test/ is consumed by `:testing:mobile:navint-tests`,
            // not the host. Kiosk features stay with `:kioskComposeApp`.
            listOf("mobile", "shared").forEach { grouping ->
                rootDir.resolve("features/$grouping").walkTopDown()
                    .filter { dir -> dir.isDirectory && dir.resolve("build.gradle.kts").exists() }
                    .filter { !it.relativeTo(rootDir).path.endsWith("/test") }
                    .forEach { implementation(project(":" + it.relativeTo(rootDir).path.replace("/", ":"))) }
            }

            // Core
            api(project(":core:circuit"))
            api(project(":core:metro"))
            implementation(project(":core:analytics:impl"))
            implementation(project(":core:auth:impl"))
            api(project(":core:remote-config:impl"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:circuit"))
            implementation(project(":core:theme"))
            implementation(project(":core:network:impl"))
            implementation(project(":core:build-config:impl"))
            implementation(project(":core:persistence:impl"))
            implementation(project(":core:logger:impl"))

            // Circuit
            implementation(libs.circuit.foundation)
            implementation(libs.circuit.runtime)
            implementation(libs.circuit.runtime.presenter)
            implementation(libs.circuit.runtime.ui)
            implementation(libs.circuit.retained)
            implementation(libs.circuitx.gesture.navigation)

            // Metro DI
            implementation(libs.metro.runtime)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(project(":core:analytics:test"))
            implementation(project(":core:test-fixtures"))
            implementation(libs.circuit.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.compose.material3.windowsizeclass)
        }

        iosMain.dependencies {
            // Molecule for Compose → StateFlow bridge
            implementation(libs.molecule.runtime)
            implementation(libs.nativecoroutines.core)
        }
    }
}

