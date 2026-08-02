pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Resolves the JDK that `jvmToolchain(21)` asks for, downloading one if the daemon JVM
// isn't a match. This is what replaces the machine-specific `org.gradle.java.home` that
// used to live in gradle.properties — the build now works on any machine and in CI.
// Version is a literal, not `libs.plugins.*`: settings `plugins {}` is evaluated before
// `dependencyResolutionManagement`, so the version catalog isn't available yet.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "MockDonalds"

// Build tooling (JVM-only, not part of the shipped app)
include(":build-tooling:ksp-build-config-registry")
include(":build-tooling:ksp-fake-app-build-config")

// App
include(":androidApp")
include(":composeApp")

// Core modules
include(":core:centerpost")
include(":core:test-fixtures")
include(":core:theme")
include(":core:network:api")
include(":core:network:impl")
include(":core:circuit")
include(":core:auth:api")
include(":core:auth:impl")
include(":core:presentation")
include(":core:remote-config:api")
include(":core:remote-config:impl")
include(":core:remote-config:test")
include(":core:analytics:api")
include(":core:analytics:impl")
include(":core:analytics:test")
include(":core:metro")
include(":core:build-config:api")
include(":core:build-config:impl")
include(":core:build-config:test")
include(":core:strings")
include(":core:logger:api")
include(":core:logger:impl")
include(":core:logger:test")
include(":core:persistence:api")
include(":core:persistence:impl")
include(":core:persistence:test")

// Feature modules — auto-discovered, architecture-enforced submodules.
// Debug-only features (e.g. `debug-menu`) are filtered at runtime via
// `AppBuildConfig.buildType` rather than stripped from the module graph.
rootDir.resolve("features").listFiles()
    ?.filter { it.isDirectory }
    ?.map { it.name }
    ?.sorted()
    ?.forEach { feature ->
        include(":features:$feature:api:domain")
        include(":features:$feature:api:navigation")
        include(":features:$feature:impl:data")
        include(":features:$feature:impl:domain")
        include(":features:$feature:impl:presentation")
        include(":features:$feature:test")
    }

// Testing modules
include(":testing:architecture-check")
include(":testing:navint-tests")
include(":testing:e2e-tests")
include(":testing:benchmarks")
