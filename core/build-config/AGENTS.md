# Core: Build Config

## Purpose

Compile-time market and environment configuration. Emits flat `BuildConfig` constants via BuildKonfig, baked per build from `-Pmarket=` and `-Penv=` Gradle properties. Each market+env combination produces a distinct binary with its own identity, endpoints, locale, and currency.

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
| `AppBuildConfig` | `api` | Public interface; 13 `val`s (`appName`, `appId`, `market`, `env`, `baseUrl`, `cdnUrl`, `menuBaseUrl`, `orderBaseUrl`, `accountBaseUrl`, `rewardsBaseUrl`, `storeBaseUrl`, `locale`, `currency`) |
| `AppBuildConfigImpl` | `impl` | Production binding — reads every field from the generated `BuildConfig` |
| `BuildConfig` | `impl` (generated) | Internal BuildKonfig object — never imported outside `impl` (Konsist-enforced by `BuildConfigImportTest`) |
| `FakeAppBuildConfig` | `test` | Test binding — all fields are `var`s with valid default values; override per test |

## Selection

| Input | Default | Example |
|-------|---------|---------|
| `-Pmarket` | `us` | `-Pmarket=de` |
| `-Penv`    | `int` | `-Penv=prod` |

**Markets** (5): `us`, `ca`, `de`, `au`, `core`. `core` is a synthetic sandbox market based on `us` config, useful for exercising market-neutral flows without hitting a real region's backend.

**Environments** (3): `int` (integration — engineer-facing), `mte` (manual test environment — QA sign-off), `prod` (production).

**Build types** (2 — iOS only): `Debug` (non-optimized, testability on), `Release` (optimized, obfuscation/minify on). Android handles this via Gradle's standard `assembleDebug` / `assembleRelease` tasks independently of `-Penv`. iOS couples them into the build configuration name (`US-Int-Debug`, `US-Int-Release`, etc.) so R8/minify-only regressions can be reproduced against any env.

**Matrix**: 5 markets × 3 envs × 2 build types = **30 combos on iOS**. On Android: 15 `.properties` files × 2 Gradle build types = 30 variants (no flavor declarations needed — `-P` properties select the config at configure time).

iOS forwards `MARKET` and `ENV` via xcconfig — the Gradle build phase reads them from the env and passes them through. The Xcode project itself (`iosApp/iosApp.xcodeproj`) is **generated from `iosApp/project.yml` by xcodegen**; see `iosApp/AGENTS.md` for the project-generation workflow.

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

`impl/build.gradle.kts` reads `Defaults.properties`, overlays `markets/$market/$market-$env.properties`, and passes the merged map to `buildkonfig { defaultConfigs { } }`. An unknown combo fails configuration with a clear error. Plain `.properties` format (not `.gradle.kts`) is used because scripts applied via `apply(from = …)` do not inherit the plugin classpath — `.properties` keeps per-combo files dead-simple: no imports, no Kotlin, just key=value. Markets are grouped under per-market subfolders so the directory scales cleanly as envs and markets grow.

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
2. Add 6 iOS xcconfigs in `iosApp/Configuration/{market}/` (`{MARKET}-{Env}-{Debug,Release}.xcconfig` — 3 envs × 2 build types).
3. Update `iosApp/project.yml`: add 6 entries under top-level `configs:` (Debug→`debug`, Release→`release`) and 6 `configFiles:` entries under each of the three targets (`iosApp`, `iosAppTests`, `iosAppE2ETests`). Pattern is already established — copy an existing market's block.
4. Regenerate the Xcode project: `cd iosApp && xcodegen generate`. Verify with `xcodebuild -list` (should show 6 new configurations).
5. Add the market to the CI matrix.
6. Smoke build both platforms:
   ```
   ./gradlew :androidApp:assembleDebug -Pmarket={market} -Penv=int
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iOSApp -configuration {MARKET}-Int-Debug -destination 'generic/platform=iOS Simulator' -sdk iphonesimulator build
   ```

The `add-market` skill (`.agents/skills/add-market/SKILL.md`) automates steps 1–4. Full spec lives in `.agents/standards/build-config.md`.

## Adding a new field

1. Add the key to `impl/Defaults.properties` with a safe default.
2. Override per combo in each `impl/markets/*.properties` where it differs.
3. Expose it on `AppBuildConfig` in `api/src/commonMain/kotlin/…/AppBuildConfig.kt`.
4. Override it in `AppBuildConfigImpl` in `impl/src/commonMain/kotlin/…/AppBuildConfigImpl.kt`.
5. Add a sensible default + corresponding `var` override in `FakeAppBuildConfig` (`test/src/commonMain/…`) so test graphs stay green.
6. Consume via `AppBuildConfig.fieldName` (injected via Metro).

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
