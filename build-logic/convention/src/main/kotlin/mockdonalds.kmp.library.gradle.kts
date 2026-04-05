import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

kotlin {
    androidLibrary {
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
            baseName = project.path.replace(":", "-").removePrefix("-")
            isStatic = true
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
                                "com.mockdonalds.app.core.common.Parcelize",
                        )
                    }
                }
            }
        }
    }
}
