# Build Logic Convention Plugins

## Overview

Convention plugins in `build-logic/convention/src/main/kotlin/` standardize module configuration. Every shared module applies one of these plugins. Changes here affect all downstream modules immediately.

**WARNING: Modifying these plugins has project-wide impact. Test changes against the full build before merging.**

## Plugins

### sampleplatter.kmp.library.gradle.kts

Base plugin applied by all shared KMP modules. Provides:
- Kotlin Multiplatform with Android (compileSdk 36, minSdk 26, JVM 17) and iOS targets (x64, arm64, simulatorArm64)
- Parcelize with custom annotation (`com.jkjamies.sampleplatter.core.circuit.Parcelize`)
- KSP, Kotest, and Detekt
- commonTest dependencies: kotlin-test, kotest-framework-engine, kotest-assertions-core, kotlinx-coroutines-test, turbine
- Auto-adds `:core:test-fixtures` to every module's commonTest (except itself)
- Auto-generates per-module Kotest `ProjectConfig` subclass for native KSP discovery
- androidHostTest with JUnit Platform (Kotest runner)

### sampleplatter.kmp.domain.gradle.kts

For `impl/domain` modules. Applies:
- `sampleplatter.kmp.library`
- Metro DI (`dev.zacsweers.metro`)

### sampleplatter.kmp.data.gradle.kts

For `impl/data` modules. Applies:
- `sampleplatter.kmp.library`
- Metro DI
- Kotlin Serialization

### sampleplatter.kmp.presentation.gradle.kts

For `impl/presentation` modules. Applies:
- `sampleplatter.kmp.library`
- Compose Multiplatform + Compose Compiler
- Metro DI with Circuit codegen enabled (`enableCircuitCodegen.set(true)`)
- Circuit dependencies: foundation, runtime-presenter, retained, codegen-annotations, circuit-test (commonTest)
- Android Compose UI: foundation, material3, ui, coil-compose
- `core:strings` on `androidMain` (auto-wired so every feature can call `stringResource(R.string.…)` without per-module config)
- androidDeviceTest: compose-ui-test-junit4, core:test-fixtures
- Android device test instrumentation runner configured

### sampleplatter.phrase.gradle.kts

Applied only to `core:strings`. Registers the `pullTranslations` Gradle task (typed
`PhraseTranslationTask` in `com.jkjamies.sampleplatter.buildlogic`). The task pulls translations
from Phrase and writes:

- Android XML to `core/strings/src/androidMain/res/values{-locale}/strings.xml`
- iOS `.strings` to `iosApp/iosApp/Resources/{locale}.lproj/Localizable.strings`

Inputs (all optional Gradle properties / env vars):

- `phrase.projectId` — the Phrase project identifier
- `phrase.apiToken` (or `PHRASE_API_TOKEN` env var) — auth token, never committed
- `-Pmarket=…` — selects the market when Phrase is organized per market

The task currently throws a clear "skeleton — not wired to Phrase API" error;
implementing the HTTP call is the next milestone for localization. Inputs and outputs
are declared with `@Input` / `@OutputDirectory`, which lets Gradle skip re-runs via
up-to-date checks within a workspace. Build-cache participation (cross-workspace
sharing) requires `@CacheableTask`; add that once the Phrase API call is wired and
output is deterministic.

### sampleplatter.detekt.gradle.kts

Applied transitively via `sampleplatter.kmp.library`. Configures:
- Detekt with `config/detekt/detekt.yml`
- Builds upon default config, parallel execution, auto-correct enabled
- Excludes `/build/` directories
- Adds detekt-formatting plugin

## Module Type to Plugin Mapping

| Module Type | Plugin | Example Path |
|-------------|--------|-------------|
| api/domain | `sampleplatter.kmp.library` | features/home/api/domain |
| api/navigation | `sampleplatter.kmp.library` | features/home/api/navigation |
| impl/domain | `sampleplatter.kmp.domain` | features/home/impl/domain |
| impl/data | `sampleplatter.kmp.data` | features/home/impl/data |
| impl/presentation | `sampleplatter.kmp.presentation` | features/home/impl/presentation |
| core/* | `sampleplatter.kmp.library` | core/circuit, core/theme |
| core/strings | `sampleplatter.kmp.library` + `sampleplatter.phrase` | core/strings |
| test modules | `sampleplatter.kmp.domain` | features/home/test |
