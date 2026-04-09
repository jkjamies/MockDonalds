# Build Logic Convention Plugins

## Overview

Convention plugins in `build-logic/convention/src/main/kotlin/` standardize module configuration. Every shared module applies one of these plugins. Changes here affect all downstream modules immediately.

**WARNING: Modifying these plugins has project-wide impact. Test changes against the full build before merging.**

## Plugins

### mockdonalds.kmp.library.gradle.kts

Base plugin applied by all shared KMP modules. Provides:
- Kotlin Multiplatform with Android (compileSdk 36, minSdk 26, JVM 17) and iOS targets (x64, arm64, simulatorArm64)
- Parcelize with custom annotation (`com.mockdonalds.app.core.circuit.Parcelize`)
- KSP, Kotest, and Detekt
- commonTest dependencies: kotlin-test, kotest-framework-engine, kotest-assertions-core, kotlinx-coroutines-test, turbine
- Auto-adds `:core:test-fixtures` to every module's commonTest (except itself)
- Auto-generates per-module Kotest `ProjectConfig` subclass for native KSP discovery
- androidHostTest with JUnit Platform (Kotest runner)

### mockdonalds.kmp.domain.gradle.kts

For `impl/domain` modules. Applies:
- `mockdonalds.kmp.library`
- Metro DI (`dev.zacsweers.metro`)

### mockdonalds.kmp.data.gradle.kts

For `impl/data` modules. Applies:
- `mockdonalds.kmp.library`
- Metro DI
- Kotlin Serialization

### mockdonalds.kmp.presentation.gradle.kts

For `impl/presentation` modules. Applies:
- `mockdonalds.kmp.library`
- Compose Multiplatform + Compose Compiler
- Metro DI with Circuit codegen enabled (`enableCircuitCodegen.set(true)`)
- Circuit dependencies: foundation, runtime-presenter, retained, codegen-annotations, circuit-test (commonTest)
- Android Compose UI: foundation, material3, ui, coil-compose
- androidDeviceTest: compose-ui-test-junit4, core:test-fixtures
- Android device test instrumentation runner configured

### mockdonalds.detekt.gradle.kts

Applied transitively via `mockdonalds.kmp.library`. Configures:
- Detekt with `config/detekt/detekt.yml`
- Builds upon default config, parallel execution, auto-correct enabled
- Excludes `/build/` directories
- Adds detekt-formatting plugin

## Module Type to Plugin Mapping

| Module Type | Plugin | Example Path |
|-------------|--------|-------------|
| api/domain | `mockdonalds.kmp.library` | features/home/api/domain |
| api/navigation | `mockdonalds.kmp.library` | features/home/api/navigation |
| impl/domain | `mockdonalds.kmp.domain` | features/home/impl/domain |
| impl/data | `mockdonalds.kmp.data` | features/home/impl/data |
| impl/presentation | `mockdonalds.kmp.presentation` | features/home/impl/presentation |
| core/* | `mockdonalds.kmp.library` | core/circuit, core/theme |
| test modules | `mockdonalds.kmp.domain` | features/home/test |
