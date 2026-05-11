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

// SQLDelight schema declaration is owned by the contributing feature module —
// `features/order/impl/data` applies the `app.cash.sqldelight` plugin and declares
// the application-wide `AppDatabase` (packageName = "com.mockdonalds.app.persistence").
// composeApp imports the generated class via its existing `implementation(project(...))`
// dependency on the feature and instantiates `AppDatabase(driver)` in `DatabaseProviders`.
//
// When a SECOND feature starts contributing tables, switch to the SQLDelight cross-module
// aggregation pattern: have composeApp re-declare `databases { create("AppDatabase") }`
// with `dependency(project(...))` for each contributing feature, and add
// `evaluationDependsOnChildren()` in settings.gradle.kts so contributor SQLDelight
// plugins are applied before composeApp's `dependency()` call resolves.

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

            // Export feature modules for iOS consumption (auto-discovered).
            rootDir.resolve("features").listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.sorted()
                ?.forEach { feature ->
                    export(project(":features:$feature:api:domain"))
                    export(project(":features:$feature:api:navigation"))
                    export(project(":features:$feature:impl:presentation"))
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

            // Feature modules (auto-discovered, architecture-enforced wiring).
            rootDir.resolve("features").listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.sorted()
                ?.forEach { feature ->
                    api(project(":features:$feature:api:domain"))
                    api(project(":features:$feature:api:navigation"))
                    implementation(project(":features:$feature:impl:data"))
                    implementation(project(":features:$feature:impl:domain"))
                    api(project(":features:$feature:impl:presentation"))
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

