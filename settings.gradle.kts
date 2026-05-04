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

// Feature modules — auto-discovered, architecture-enforced submodules grouped by app surface.
//
// `features/mobile/{name}/...`  — consumer mobile/iOS app features (10 features).
// `features/kiosk/{name}/...`   — kiosk app features (3 features).
// `features/shared/{name}/...`  — features whose screens/presenters are consumed by both apps
//                                  (empty for now; populate when a feature's UI is genuinely multi-host).
//
// Konsist enforces the cross-app boundary at the UI-surface layer (api/navigation +
// impl/presentation) rather than the feature directory level — kiosk can pull mobile/*'s
// api/domain + impl/{data,domain} for domain reuse (e.g., the menu domain), it just
// cannot import mobile/*/api/navigation Screens or mobile/*/impl/presentation. Symmetric
// for the reverse direction. See KioskBoundaryTest in :testing:architecture-check.
//
// Debug-only features (e.g. `debug-menu`) are filtered at runtime via
// `AppBuildConfig.buildType` rather than stripped from the module graph.
// Walk every feature group and include any directory that carries a build.gradle.kts.
// A feature may carry any subset of the 6 submodules (`api/{domain,navigation}`,
// `impl/{data,domain,presentation}`, `test`) — for example, `features/shared/menu/`
// has only the data layer because its screen lives in the consuming app's feature.
// No hardcoded submodule list — if a feature adds a new submodule, the walk picks it up.
listOf("mobile", "kiosk", "shared").forEach { grouping ->
    rootDir.resolve("features/$grouping").walkTopDown()
        .filter { it.isDirectory && it.resolve("build.gradle.kts").exists() }
        .forEach { include(":" + it.relativeTo(rootDir).path.replace("/", ":")) }
}

// Testing modules — `architecture-check` is project-wide (Konsist scans every
// module). The per-app suites mirror the features/ grouping: `testing/mobile/*`
// targets `:androidApp`, `testing/kiosk/*` targets `:kioskApp`. There is no
// `testing/shared/*` yet; if shared features ever ship a screen+presenter that's
// used by both apps, both `testing/mobile/*` and `testing/kiosk/*` cover them.
include(":testing:architecture-check")
include(":testing:mobile:navint-tests")
include(":testing:mobile:e2e-tests")
include(":testing:mobile:benchmarks")
include(":testing:kiosk:navint-tests")
include(":testing:kiosk:e2e-tests")
include(":testing:kiosk:benchmarks")
