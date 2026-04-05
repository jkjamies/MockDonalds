plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
    alias(libs.plugins.kmp.nativecoroutines)
}

ksp {
    arg("circuit.codegen.mode", "metro")
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

            // Export feature APIs for iOS consumption
            export(project(":features:home:api"))
            export(project(":features:order:api"))
            export(project(":features:rewards:api"))
            export(project(":features:scan:api"))
            export(project(":features:more:api"))
            export(project(":core:common"))
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Feature APIs
            api(project(":features:home:api"))
            api(project(":features:order:api"))
            api(project(":features:rewards:api"))
            api(project(":features:scan:api"))
            api(project(":features:more:api"))

            // Feature implementations
            implementation(project(":features:home:data"))
            implementation(project(":features:home:domain"))
            implementation(project(":features:home:presentation"))
            implementation(project(":features:order:data"))
            implementation(project(":features:order:domain"))
            implementation(project(":features:order:presentation"))
            implementation(project(":features:rewards:data"))
            implementation(project(":features:rewards:domain"))
            implementation(project(":features:rewards:presentation"))
            implementation(project(":features:scan:data"))
            implementation(project(":features:scan:domain"))
            implementation(project(":features:scan:presentation"))
            implementation(project(":features:more:data"))
            implementation(project(":features:more:domain"))
            implementation(project(":features:more:presentation"))

            // Core
            api(project(":core:common"))
            implementation(project(":core:theme"))
            implementation(project(":core:network"))

            // Circuit
            implementation(libs.circuit.foundation)
            implementation(libs.circuit.runtime)
            implementation(libs.circuit.runtime.presenter)
            implementation(libs.circuit.runtime.ui)
            implementation(libs.circuit.retained)
            implementation(libs.circuit.codegen.annotations)

            // Metro DI
            implementation(libs.metro.runtime)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
        }

        iosMain.dependencies {
            // Molecule for Compose → StateFlow bridge
            implementation(libs.molecule.runtime)
            implementation(libs.nativecoroutines.core)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.circuit.codegen)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
