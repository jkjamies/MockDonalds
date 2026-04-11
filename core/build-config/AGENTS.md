# Core: Build Config

## Purpose

Compile-time market and environment configuration. Emits flat `BuildConfig` constants via BuildKonfig, baked per build from `-Pmarket=` and `-Penv=` Gradle properties. Each market+env combination produces a distinct binary with its own identity, endpoints, locale, and currency.

## Key Types

| Type | Role |
|------|------|
| `AppBuildConfig` | Public facade in `com.mockdonalds.app.core.buildconfig` exposing `appName`, `market`, `env`, `baseUrl`, `cdnUrl`, `locale`, `currency` |
| `BuildConfig` | Internal BuildKonfig-generated object (not consumed directly — use `AppBuildConfig`) |

## Selection

| Input | Default | Example |
|-------|---------|---------|
| `-Pmarket` | `us` | `-Pmarket=de` |
| `-Penv`    | `dev` | `-Penv=prod` |

iOS forwards both via xcconfig (`MARKET`, `ENV`) — the Gradle build phase reads them from the env and passes them through.

## Layout

```
core/build-config/
  build.gradle.kts             applies BuildKonfig, reads -Pmarket / -Penv, merges Defaults + combo
  Defaults.properties          shared defaults for every combo
  markets/
    us-dev.properties          us + dev overrides
    us-prod.properties         us + prod
    de-dev.properties          de + dev
    de-prod.properties         de + prod
  src/commonMain/kotlin/       AppBuildConfig public facade
```

`build.gradle.kts` reads `Defaults.properties`, overlays `markets/$market-$env.properties`, and passes the merged map to `buildkonfig { defaultConfigs { } }`. An unknown combo fails configuration with a clear error. Plain `.properties` format (not `.gradle.kts`) is used because scripts applied via `apply(from = …)` do not inherit the plugin classpath — `.properties` keeps per-combo files dead-simple: no imports, no Kotlin, just key=value.

## Boundary with Harness

| This module (compile-time)        | Harness (runtime)          |
|-----------------------------------|----------------------------|
| API base / CDN URLs (env-baked)   | Feature toggles            |
| Market code / locale / currency   | Kill switches              |
| App name                          | A/B test variants          |
|                                   | Gradual rollouts, dynamic copy |

**Rule:** if a PM might want to change it without shipping, it goes to Harness. Everything else lives here.

## Adding a new market

1. Create `markets/{market}-{env}.properties` for every env you support (e.g. `ca-dev.properties`, `ca-prod.properties`).
2. Add the market to the CI matrix.
3. Build: `./gradlew :composeApp:assembleDebug -Pmarket=ca -Penv=prod`.

## Adding a new field

1. Add the key to `Defaults.properties` with a safe default.
2. Override per combo in each `markets/*.properties` where it differs.
3. Expose it on `AppBuildConfig` in `src/commonMain/kotlin/…/AppBuildConfig.kt`.
4. Consume via `AppBuildConfig.fieldName`.

## Rules

- Must NOT depend on any feature module.
- Must NOT contain feature flags or runtime-tunable values — those belong to Harness.
- Per-combo files are plain `.properties` — no logic, only key=value.
- Consumers import `AppBuildConfig`, not the internal `BuildConfig` object.
