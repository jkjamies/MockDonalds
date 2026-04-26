import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("mockdonalds.kmp.library")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.buildconfig.api"
    }
    sourceSets.getByName("commonMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":build-tooling:ksp-build-config-registry"))
}

// KSP on a KMP module emits into kspCommonMainMetadata — make every Kotlin compile task wait for it.
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
