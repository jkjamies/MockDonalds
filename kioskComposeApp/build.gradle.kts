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

            // Kiosk feature modules — auto-discovered from features/kiosk/.
            // No-op until phase 2 lands the first kiosk feature.
            rootDir.resolve("features/kiosk").listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.sorted()
                ?.forEach { feature ->
                    api(project(":features:kiosk:$feature:api:domain"))
                    api(project(":features:kiosk:$feature:api:navigation"))
                    implementation(project(":features:kiosk:$feature:impl:data"))
                    implementation(project(":features:kiosk:$feature:impl:domain"))
                    api(project(":features:kiosk:$feature:impl:presentation"))
                }

            // Reused consumer menu domain — NOT presentation.
            api(project(":features:order:api:domain"))
            api(project(":features:order:api:navigation"))
            implementation(project(":features:order:impl:data"))
            implementation(project(":features:order:impl:domain"))

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
