plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
    alias(libs.plugins.kmp.nativecoroutines)
}

metro {
    enableCircuitCodegen.set(true)
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

            // Export feature modules for iOS consumption (auto-discovered)
            rootDir.resolve("features").listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.sorted()
                ?.forEach { feature ->
                    export(project(":features:$feature:api:domain"))
                    export(project(":features:$feature:api:navigation"))
                    export(project(":features:$feature:presentation"))
                }
            export(project(":core:common"))
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

            // Feature modules (auto-discovered, architecture-enforced wiring)
            rootDir.resolve("features").listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.sorted()
                ?.forEach { feature ->
                    api(project(":features:$feature:api:domain"))
                    api(project(":features:$feature:api:navigation"))
                    implementation(project(":features:$feature:data"))
                    implementation(project(":features:$feature:domain"))
                    api(project(":features:$feature:presentation"))
                }

            // Core
            api(project(":core:common"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
            implementation(project(":core:network"))

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

