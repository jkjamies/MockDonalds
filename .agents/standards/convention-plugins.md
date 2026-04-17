# Convention Plugins Reference

Convention plugins in `build-logic/convention/` eliminate boilerplate across 20+ feature modules. Each architectural layer has a dedicated plugin that configures KMP targets, dependencies, and tooling.

## Plugin Hierarchy

```
mockdonalds.kmp.library (base)
  ├── mockdonalds.kmp.domain      = library + Metro DI
  ├── mockdonalds.kmp.data        = library + Metro DI + Serialization
  └── mockdonalds.kmp.presentation = library + Compose + Metro DI + Circuit codegen + Coil

mockdonalds.detekt (applied transitively via library)
```

## Critical: What NOT to Do in Feature build.gradle.kts

Convention plugins handle nearly everything. Feature `build.gradle.kts` files should be minimal — just the plugin ID and dependencies. Specifically:

- **Do NOT declare `android { namespace = ... }`** — the convention plugin derives it automatically from the module path
- **Do NOT add test framework dependencies** (Kotest, Turbine, coroutines-test) — provided by `mockdonalds.kmp.library`
- **Do NOT add serialization dependencies in `impl/domain` or `api/*`** — only `mockdonalds.kmp.data` includes serialization
- **Do NOT add Compose, Circuit, or Coil dependencies** — provided by `mockdonalds.kmp.presentation`
- **Do NOT add Metro/DI dependencies** — provided by `mockdonalds.kmp.domain`, `.data`, and `.presentation`

A typical feature `impl/presentation/build.gradle.kts` looks like:

```kotlin
plugins { id("mockdonalds.kmp.presentation") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:{name}:api:domain"))
            implementation(project(":features:{name}:api:navigation"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:theme"))
        }
        commonTest.dependencies {
            implementation(project(":features:{name}:test"))
        }
    }
}
```

No `android {}` block, no library dependencies — just project dependencies.

## mockdonalds.kmp.library

Base KMP library plugin. Used by `api/domain`, `api/navigation`, and `core/*` modules.

Provides:
- KMP targets: Android (`com.android.kotlin.multiplatform.library`), iOS (x64, arm64, simulator)
- Android SDK: compileSdk 36, minSdk 26, JVM target 17
- Android host tests (`withHostTest { isReturnDefaultValues = true }`)
- Kotest 6.1.11 + KSP for native test discovery (`io.kotest` Gradle plugin)
- Parcelize support
- All `commonTest` dependencies: kotest-framework-engine, kotest-assertions-core, kotlinx-coroutines-test, turbine
- Auto-generated `KotestProjectConfig` per module (for native KSP discovery)
- `core:test-fixtures` as `commonTest` dependency (except for test-fixtures itself)
- `kotest-runner-junit6` on `androidHostTest` with JUnit Platform configuration
- Detekt (via transitive `mockdonalds.detekt`)

## mockdonalds.kmp.domain

```kotlin
plugins {
    id("mockdonalds.kmp.library")
    id("dev.zacsweers.metro")           // Compile-time DI
}
```

For `impl/domain` and `test` modules. Use case implementations use `@ContributesBinding` to wire to their abstractions. Test modules use it so fakes can have `@ContributesBinding` for navint-tests DI auto-discovery.

## mockdonalds.kmp.data

```kotlin
plugins {
    id("mockdonalds.kmp.library")
    id("dev.zacsweers.metro")           // Compile-time DI
    id("org.jetbrains.kotlin.plugin.serialization")  // JSON serialization
}
```

For `impl/data` modules containing repository implementations, DTOs, and network/storage calls.

## mockdonalds.kmp.presentation

```kotlin
plugins {
    id("mockdonalds.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.zacsweers.metro")
}

metro {
    enableCircuitCodegen.set(true)      // Metro's built-in Circuit codegen
}
```

For `impl/presentation` modules. Provides:
- Compose Multiplatform runtime
- Compose Compiler plugin
- Circuit dependencies (runtime, foundation)
- Metro DI with Circuit codegen (generates `Presenter.Factory` and `Ui.Factory` from `@CircuitInject`)
- `circuit-codegen-annotations` (added automatically by Metro)
- Material 3 + WindowSizeClass
- Coil for image loading on Android
- `androidDeviceTest` support:
  - `compose-ui-test-junit4` (JetBrains Compose Multiplatform)
  - `core:test-fixtures` (StateRobot base class)
  - `AndroidJUnitRunner` for instrumented tests
  - Disabled `copyAndroidDeviceTestComposeResourcesToAndroidAssets` (Compose Multiplatform 1.10.3 workaround)

## mockdonalds.detekt

Applied transitively via `mockdonalds.kmp.library`:
- Config: `config/detekt/detekt.yml`
- `buildUponDefaultConfig = true`
- Parallel execution enabled
- `autoCorrect = true`
- `detekt-formatting` plugin (ktlint rules)

## Auto-Wiring in settings.gradle.kts

Feature modules are auto-discovered via filesystem traversal -- no manual `include()` per feature:

```kotlin
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
```

## Auto-Wiring in composeApp/build.gradle.kts

Dependencies use the same filesystem scan with enforced visibility:

```
api(project(":features:$feature:api:domain"))          -- models visible to iOS framework
api(project(":features:$feature:api:navigation"))       -- screens visible to iOS framework
implementation(project(":features:$feature:impl:data")) -- private, not exported
implementation(project(":features:$feature:impl:domain"))-- private, not exported
api(project(":features:$feature:impl:presentation"))    -- exported for iOS framework
```

iOS framework export loop separately exports `api:domain`, `api:navigation`, and `impl:presentation` for each feature, plus `core:circuit`.

Adding a new feature directory under `features/` automatically includes all 6 submodules AND wires all dependencies. Zero manual configuration needed.
