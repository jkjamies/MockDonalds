# Core: Build Config

## Purpose

Compile-time market, environment, and build-type configuration. Emits flat `BuildConfig` constants via BuildKonfig, baked per build from three signals (explicit `-Pmarket`/`-Penv`/`-PbuildType`, AGP variant task names like `assembleUsIntDebug`, then defaults) resolved by the shared `BuildVariantResolver` in `build-logic/convention/src/main/kotlin/com/mockdonalds/buildlogic/BuildVariantResolver.kt`. Each market+env combination produces a distinct binary with its own identity, endpoints, locale, and currency; `buildType` flows through the same pipeline to let runtime code distinguish debug from release.

## Module structure (api / impl / test)

Split into three leaf modules matching `core:feature-flag`, `core:analytics`, `core:auth`:

| Module | Gradle coordinate | Contains | Depended on by |
|---|---|---|---|
| **api** | `:core:build-config:api` | `AppBuildConfig` interface — the sole consumer surface | Every module that injects `AppBuildConfig` (features, `core:network:impl`, `core:metro`) |
| **impl** | `:core:build-config:impl` | BuildKonfig plugin, `Defaults.properties`, `markets/`, `AppBuildConfigImpl` (`@ContributesBinding(AppScope::class)`), `validateAllMarkets` Gradle task, smoke test `AppBuildConfigTest` | `composeApp` only — it wires the Metro binding into `ProdAppGraph` |
| **test** | `:core:build-config:test` | `FakeAppBuildConfig` (`@ContributesBinding(AppScope::class)` with sensible defaults) | Test graphs in modules that swap out `AppBuildConfig` |

The api module is contract-only (no Metro, no BuildKonfig). Only `impl` rebuilds when a `.properties` file changes or `-Pmarket` flips, so feature-module compile caches are stable across combo switches.

## Key Types

| Type | Where | Role |
|------|-------|------|
| `AppBuildConfig` | `api` | Public interface; 14 `val`s (`appName`, `appId`, `market`, `env`, `buildType`, `baseUrl`, `cdnUrl`, `menuBaseUrl`, `orderBaseUrl`, `accountBaseUrl`, `rewardsBaseUrl`, `storeBaseUrl`, `locale`, `currency`) plus the extension `val AppBuildConfig.isDebug: Boolean get() = buildType == "debug"` |
| `BuildConfigField` | `api` | Data class (`name`, `value`, `group: Group`) plus enumeration extension `fun AppBuildConfig.asFields(): List<BuildConfigField>`. Powers the debug-menu build-config viewer and any future read-only dumpers. `Group` is `Identity`, `Urls`, or `Localization`. Completeness is Konsist-enforced (`BuildConfigCoverageTest`) — every property on the interface must appear in `asFields()`. |
| `AppBuildConfigImpl` | `impl` | Production binding — reads every field from the generated `BuildConfig` |
| `BuildConfig` | `impl` (generated) | Internal BuildKonfig object — never imported outside `impl` (Konsist-enforced by `BuildConfigImportTest`) |
| `FakeAppBuildConfig` | `test` | Test binding — all fields are `var`s with valid default values (`buildType = "debug"` by default); override per test |

## Selection

Both KMP build files — `core/build-config/impl/build.gradle.kts` and `core/feature-flag/impl/build.gradle.kts` — delegate to the shared `BuildVariantResolver` (`build-logic/convention/src/main/kotlin/com/mockdonalds/buildlogic/BuildVariantResolver.kt`). Three-rung resolution chain:

| Rung | Signal | Who sends it |
|---|---|---|
| 1 | `-Pmarket` / `-Penv` / `-PbuildType` | iOS preBuildScript (from active xcconfig), CI, ad-hoc CLI |
| 2 | AGP variant task names like `assembleUsIntDebug` | Android Studio's Build Variants window + `./gradlew :androidApp:assemble*` |
| 3 | Defaults `us` / `int` / `debug` | Fallback when no signal is present |

**Markets** (5): `us`, `ca`, `de`, `au`, `core`. `core` is a synthetic sandbox market based on `us` config, useful for exercising market-neutral flows without hitting a real region's backend.

**Environments** (3): `int` (integration — engineer-facing), `mte` (manual test environment — QA sign-off), `prod` (production).

**Build types** (2): `debug` (non-optimized, testability on), `release` (optimized, obfuscation/minify on). Android declares both natively in `androidApp/build.gradle.kts` `buildTypes {}` and crosses them with `flavorDimensions = ["market", "env"]` for 30 native Build Variants; iOS couples `{market}-{env}-{buildType}` into the xcconfig name (`US-Int-Debug`, `US-Int-Release`) and forwards `$KOTLIN_FRAMEWORK_BUILD_TYPE` to Gradle as `-PbuildType` in the pre-build script. `AppBuildConfig.buildType` reads the same value at runtime, so any KMP code can gate behavior via `appBuildConfig.isDebug` without platform-specific shims.

**Matrix**: 5 markets × 3 envs × 2 build types = **30 combos on both platforms**. iOS materializes them as 30 `XCBuildConfiguration` entries; Android materializes them as AGP product-flavor × buildType combinations, each appearing as a real Gradle task (`assembleUsIntDebug`, `assembleDeProdRelease`, …). The 15 `.properties` files in `impl/markets/` supply values for the market+env axis; debug/release is handled at the consumer level.

iOS forwards `MARKET`, `ENV`, and `KOTLIN_FRAMEWORK_BUILD_TYPE` via xcconfig — the Gradle build phase reads them from the env and passes `-Pmarket`, `-Penv`, and `-PbuildType` through (rung 1). Android Studio's Build Variants picker drives rung 2 automatically via task-name parsing. The Xcode project itself (`iosApp/iosApp.xcodeproj`) is **generated from `iosApp/project.yml` by xcodegen**; see `iosApp/AGENTS.md` for the project-generation workflow.

## Layout

```
core/build-config/
  AGENTS.md                           this file
  api/
    build.gradle.kts                  kmp.library (contract-only, no Metro)
    src/commonMain/kotlin/            AppBuildConfig interface
  impl/
    build.gradle.kts                  kmp.domain + BuildKonfig + validateAllMarkets
    Defaults.properties               shared defaults for every combo
    markets/
      us/us-int.properties            us + int
      us/us-mte.properties            us + mte
      us/us-prod.properties           us + prod
      ca/ca-int.properties, ca-mte.properties, ca-prod.properties
      de/de-int.properties, de-mte.properties, de-prod.properties
      au/au-int.properties, au-mte.properties, au-prod.properties
      core/core-int.properties, core-mte.properties, core-prod.properties
    src/commonMain/kotlin/            AppBuildConfigImpl
    src/commonTest/kotlin/            AppBuildConfigTest (Kotest smoke)
  test/
    build.gradle.kts                  kmp.domain, api(:core:build-config:api) + api(:core:test-fixtures)
    src/commonMain/kotlin/            FakeAppBuildConfig
```

`impl/build.gradle.kts` calls `BuildVariantResolver.market(project)` / `.env(project)` / `.buildType(project)`, reads `Defaults.properties`, overlays `markets/$market/$market-$env.properties`, and passes the merged map to `buildkonfig { defaultConfigs { } }`. An unknown combo fails configuration with a clear error. Plain `.properties` format (not `.gradle.kts`) is used because scripts applied via `apply(from = …)` do not inherit the plugin classpath — `.properties` keeps per-combo files dead-simple: no imports, no Kotlin, just key=value. Markets are grouped under per-market subfolders so the directory scales cleanly as envs and markets grow.

## Boundary with Harness

| This module (compile-time)        | Harness (runtime)          |
|-----------------------------------|----------------------------|
| API base / CDN URLs (env-baked)   | Feature toggles            |
| Market code / locale / currency   | Kill switches              |
| App name                          | A/B test variants          |
|                                   | Gradual rollouts, dynamic copy |

**Rule:** if a PM might want to change it without shipping, it goes to Harness. Everything else lives here.

## Adding a new market

1. Create the subfolder `impl/markets/{market}/` and add `{market}-int.properties`, `{market}-mte.properties`, `{market}-prod.properties` (every env must exist — `validateAllMarkets` enforces symmetry).
2. Register the Android flavor in `androidApp/build.gradle.kts`: `create("{market}") { dimension = "market"; applicationIdSuffix = ".{market}" }`.
3. Extend the market alternation in the shared resolver `build-logic/convention/src/main/kotlin/com/mockdonalds/buildlogic/BuildVariantResolver.kt` (one regex edit — both `:core:build-config:impl` and `:core:feature-flag:impl` pick it up automatically).
4. Add 6 iOS xcconfigs in `iosApp/Configuration/{market}/` (`{MARKET}-{Env}-{Debug,Release}.xcconfig` — 3 envs × 2 build types).
5. Update `iosApp/project.yml`: add 6 entries under top-level `configs:` (Debug→`debug`, Release→`release`) and 6 `configFiles:` entries under each of the three targets (`iosApp`, `iosAppTests`, `iosAppE2ETests`). Pattern is already established — copy an existing market's block.
6. Regenerate the Xcode project: `cd iosApp && xcodegen generate`. Verify with `xcodebuild -list` (should show 6 new configurations).
7. Add the market to the CI matrix.
8. Smoke build both platforms:
   ```
   ./gradlew :androidApp:assemble{Market}IntDebug
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iOSApp -configuration {MARKET}-Int-Debug -destination 'generic/platform=iOS Simulator' -sdk iphonesimulator build
   ```

The `add-market` skill (`.agents/skills/add-market/SKILL.md`) automates steps 1–6. Full spec lives in `.agents/standards/build-config.md`.

## Adding a new field

1. Add the key to `impl/Defaults.properties` with a safe default.
2. Override per combo in each `impl/markets/*.properties` where it differs.
3. Expose it on `AppBuildConfig` in `api/src/commonMain/kotlin/…/AppBuildConfig.kt`.
4. Override it in `AppBuildConfigImpl` in `impl/src/commonMain/kotlin/…/AppBuildConfigImpl.kt`.
5. Add a sensible default + corresponding `var` override in `FakeAppBuildConfig` (`test/src/commonMain/…`) so test graphs stay green.
6. Append the new field to `asFields()` in `api/src/commonMain/kotlin/…/BuildConfigField.kt`, picking the right `Group` (`Identity`, `Urls`, or `Localization`). Without this, the debug-menu build-config viewer will not surface the field and `BuildConfigCoverageTest` will fail.
7. Consume via `AppBuildConfig.fieldName` (injected via Metro).

## Quick commands

Run these directly against `:core:build-config:impl` to validate, regenerate, or sanity-check a combo without triggering a full app build. IDE gutter icons on fenced code blocks will pick these up.

**Validate every combo (no build, ~1s warm):**
```bash
./gradlew :core:build-config:impl:validateAllMarkets
```

**Regenerate the `BuildConfig` object for a specific combo (no compile, writes to `build/generated/source/buildkonfig/…`):**
```bash
./gradlew :core:build-config:impl:generateBuildKonfig -Pmarket=us   -Penv=int
./gradlew :core:build-config:impl:generateBuildKonfig -Pmarket=ca   -Penv=int
./gradlew :core:build-config:impl:generateBuildKonfig -Pmarket=de   -Penv=prod
./gradlew :core:build-config:impl:generateBuildKonfig -Pmarket=au   -Penv=mte
./gradlew :core:build-config:impl:generateBuildKonfig -Pmarket=core -Penv=int
```

**Full combo sync (regenerate + run the smoke test that asserts on baked values):**
```bash
./gradlew :core:build-config:impl:testAndroidHostTest -Pmarket=de -Penv=prod
```

**Assemble just this module (no downstream compilation):**
```bash
./gradlew :core:build-config:api:assemble
./gradlew :core:build-config:impl:assemble -Pmarket=us -Penv=int
./gradlew :core:build-config:test:assemble
```

**Produced file location:** `core/build-config/impl/build/generated/source/buildkonfig/commonMain/com/mockdonalds/app/core/buildconfig/BuildConfig.kt` — open to inspect the baked constants for the current `-Pmarket` / `-Penv`.

## Rules

- Must NOT depend on any feature module.
- Must NOT contain feature flags or runtime-tunable values — those belong to Harness.
- Per-combo files are plain `.properties` — no logic, only key=value.
- Consumers import `AppBuildConfig` from `:core:build-config:api`, not `BuildConfig` or `AppBuildConfigImpl` (Konsist-enforced: `BuildConfigImportTest`).
- `composeApp` is the only production module that depends on `:core:build-config:impl` (it wires the binding into `ProdAppGraph`).
