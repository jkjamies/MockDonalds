import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.buildconfig.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:build-config:api"))
            api(project(":core:test-fixtures"))
        }
        getByName("commonMain") {
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":build-tooling:ksp-fake-app-build-config"))
}

// KSP on a KMP module emits into kspCommonMainMetadata — make every Kotlin compile task wait for it.
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
