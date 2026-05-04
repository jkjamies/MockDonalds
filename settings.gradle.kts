pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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

// Apps
include(":androidApp")
include(":composeApp")
include(":kioskApp")
include(":kioskComposeApp")

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
// Top-level features under `features/` are consumer-app features. Kiosk-only
// features live nested under `features/kiosk/` and are walked separately below
// (kept disjoint from the consumer host's auto-discovery in composeApp).
// Debug-only features (e.g. `debug-menu`) are filtered at runtime via
// `AppBuildConfig.buildType` rather than stripped from the module graph.
rootDir.resolve("features").listFiles()
    ?.filter { it.isDirectory && it.name != "kiosk" }
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

// Kiosk feature modules — auto-discovered under features/kiosk/{name}/...
// Hosted exclusively by `kioskComposeApp`. Konsist enforces that consumer
// composeApp cannot import these and vice-versa.
rootDir.resolve("features/kiosk").listFiles()
    ?.filter { it.isDirectory }
    ?.map { it.name }
    ?.sorted()
    ?.forEach { feature ->
        include(":features:kiosk:$feature:api:domain")
        include(":features:kiosk:$feature:api:navigation")
        include(":features:kiosk:$feature:impl:data")
        include(":features:kiosk:$feature:impl:domain")
        include(":features:kiosk:$feature:impl:presentation")
        include(":features:kiosk:$feature:test")
    }

// Testing modules
include(":testing:architecture-check")
include(":testing:navint-tests")
include(":testing:e2e-tests")
include(":testing:benchmarks")

// Kiosk-specific testing modules — peer suites that depend on the kiosk feature
// graph and instrument against `:kioskApp`. Konsist rules covering kiosk live in
// `:testing:architecture-check`'s kiosk subpackage; no separate module needed.
include(":testing:kiosk:navint-tests")
include(":testing:kiosk:e2e-tests")
include(":testing:kiosk:benchmarks")
