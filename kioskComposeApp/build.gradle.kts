plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
    alias(libs.plugins.sqldelight)
}

metro {
    enableCircuitCodegen.set(true)
}

// SQLDelight aggregator — kioskComposeApp owns the kiosk's `AppDatabase`,
// separate from the consumer's. Kiosk runs in a separate Android sandbox
// (different applicationId), so kiosk and consumer can't share SQLite anyway.
// Kiosk-only schemas live under features/kiosk/{name}/impl/data/sqldelight/.
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.mockdonalds.kiosk.persistence")
        }
    }
}

kotlin {
    android {
        namespace = "com.mockdonalds.kiosk.composeapp"
        compileSdk = 36
        minSdk = 26

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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

            // Kiosk + shared feature modules — pick up every Gradle submodule under
            // `features/{kiosk,shared}/`. Host is an app entry point, no transitive
            // consumers, so `implementation` for all. test/ goes to navint-tests,
            // not the host. No mobile/* dependency: cross-host sharing goes through
            // `features/shared/`. KioskBoundaryTest enforces.
            listOf("kiosk", "shared").forEach { grouping ->
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
    }
}
