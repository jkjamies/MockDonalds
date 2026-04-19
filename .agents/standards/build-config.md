# Build Config Standard

Compile-time market + environment configuration lives in `core:build-config` (split into `api` / `impl` / `test` modules) and is emitted via the BuildKonfig Gradle plugin applied only in `:core:build-config:impl`. This doc is the source of truth for how the module is structured, how to add a field, and how enforcement works.

## What belongs here vs. Harness

| `core:build-config` (compile-time, baked per binary) | Harness (runtime) |
|---|---|
| API base / CDN URLs (per env) | Feature toggles |
| Market code, locale, currency, app name | Kill switches |
| Legal URLs, minimum age, support email | A/B test variants |
| Any value that defines the binary's identity | Anything a PM might want to change without shipping |

**Rule:** if flipping it requires a new store submission, it belongs here. If it can be flipped via server config, it belongs in Harness.

## Selection

| Property | Default | Example |
|---|---|---|
| `-Pmarket` | `us` | `-Pmarket=de` |
| `-Penv`    | `int` | `-Penv=prod` |

### Markets

| Market | Scope | TLD | Locale | Currency |
|---|---|---|---|---|
| `us` | United States | `.com` | `en-US` | `USD` |
| `ca` | Canada | `.ca` | `en-CA` | `CAD` |
| `de` | Germany | `.de` | `de-DE` | `EUR` |
| `au` | Australia | `.com.au` | `en-AU` | `AUD` |
| `core` | Synthetic sandbox (based on us) | `.com` with `core-` host prefix | `en-US` | `USD` |

`core` is a non-ISO market — a sandbox variant of `us` for exercising market-neutral flows against isolated backends. Its MARKET code is `core` (4 letters); the validator's market regex accepts any lowercase letters (not strictly ISO 3166-1 alpha-2) to accommodate synthetic markets.

### Environments

| Env | Purpose |
|---|---|
| `int` | Integration — engineers; live backend for in-progress features |
| `mte` | Manual Test Environment — QA sign-off; release-complete code pointed at a stable test backend |
| `prod` | Production — end users |

### Build types (iOS only — explicit in the config name)

| Build type | Compile mode | When to use |
|---|---|---|
| `Debug` | Non-optimized; testability on; `SWIFT_ACTIVE_COMPILATION_CONDITIONS = DEBUG` | Day-to-day dev |
| `Release` | `-O` + whole-module; obfuscation/minify behavior matches prod | Reproducing R8/minify-only regressions; perf/archive builds |

Android handles debug/release via Gradle's standard build types (`assembleDebug` / `assembleRelease`) — no extra declaration in `androidApp/build.gradle.kts`. iOS couples the build type into the build configuration *name* (`US-Int-Debug` vs `US-Int-Release`) because Xcode's configuration axis serves double duty as env + mode; each combo needs its own `XCBuildConfiguration`.

### The matrix

**5 markets × 3 envs × 2 build types = 30 combos.** iOS declares all 30 in `iosApp.xcodeproj` (via 30 xcconfigs and 90 `XCBuildConfiguration` entries across 3 targets — iosApp, iosAppTests, iosAppE2ETests — plus 30 project-level configs = 120 total). Android gets 30 variants from 15 `.properties` files × 2 Gradle build types — no flavor declarations required because `-P` properties select the market+env at configure time.

**Why the full matrix:** minification, resource shrinking, and obfuscation only run in release builds and often break things that worked in debug. Having `{market}-{env}-release` on every non-prod env lets the team reproduce obfuscation issues against int/mte without cutting a prod RC.

### Platform wiring

**Android:** `./gradlew :androidApp:assembleRelease -Pmarket=de -Penv=prod`. `applicationId` is derived in `androidApp/build.gradle.kts` as `com.mockdonalds.app.$market`, so every market produces a distinct Play Store app.

**iOS:** `iosApp.xcodeproj` is generated from `iosApp/project.yml` by **xcodegen** — `project.yml` is the source of truth, `project.pbxproj` is a build artifact. Each combo has its own `.xcconfig` in `iosApp/Configuration/{market}/` setting `MARKET`, `ENV`, and `KOTLIN_FRAMEWORK_BUILD_TYPE`. The iosApp target has one build configuration per combo — e.g. `US-Int-Debug`, `US-Int-Release`, `US-Prod-Release`, `DE-Mte-Debug`, `CORE-Prod-Debug`. The `iOSApp` shared scheme defaults Run/Test/Analyze to `US-Int-Debug` and Profile/Archive to `US-Prod-Release`. The Gradle build phase reads `$MARKET` / `$ENV` from the active xcconfig and forwards them via `-Pmarket=` / `-Penv=`. `PRODUCT_BUNDLE_IDENTIFIER` in `Base.xcconfig` is `com.mockdonalds.app.$(MARKET)` so every market gets a distinct App Store listing.

**Regenerating the project** (after editing `project.yml` or adding/removing xcconfigs): `cd iosApp && xcodegen generate`. Commit both `project.yml` and the regenerated `project.pbxproj` in the same commit.

**Switching locally (iOS):** `Product → Scheme → Edit Scheme → Run → Build Configuration`.

## Layout

```
core/build-config/
  api/
    build.gradle.kts                          kmp.library; zero runtime deps — pure interface surface
    src/commonMain/kotlin/.../AppBuildConfig.kt      public facade interface — the ONLY consumer surface
  impl/
    build.gradle.kts                          applies BuildKonfig + validateAllMarkets; merges Defaults + combo; emits internal BuildConfig
    Defaults.properties                       shared defaults, every field MUST have an entry here
    markets/
      us/us-int.properties, us-mte.properties, us-prod.properties
      ca/ca-int.properties, ca-mte.properties, ca-prod.properties
      de/de-int.properties, de-mte.properties, de-prod.properties
      au/au-int.properties, au-mte.properties, au-prod.properties
      core/core-int.properties, core-mte.properties, core-prod.properties
    src/commonMain/kotlin/.../AppBuildConfigImpl.kt  binds the facade; reads from generated internal BuildConfig
    src/commonTest/kotlin/.../AppBuildConfigTest.kt  Phase 1 smoke test, must reference every field
  test/
    build.gradle.kts                          kmp.domain; depends on :core:build-config:api
    src/commonMain/kotlin/.../test/FakeAppBuildConfig.kt   @ContributesBinding test double, mutable var fields

iosApp/Configuration/
  Base.xcconfig                               shared base (deployment target, bundle-id = com.mockdonalds.app.$(MARKET))
  us/  US-Int-Debug.xcconfig, US-Int-Release.xcconfig, US-Mte-Debug.xcconfig, US-Mte-Release.xcconfig, US-Prod-Debug.xcconfig, US-Prod-Release.xcconfig
  ca/  CA-*-*.xcconfig × 6
  de/  DE-*-*.xcconfig × 6
  au/  AU-*-*.xcconfig × 6
  core/CORE-*-*.xcconfig × 6
```

Properties and xcconfigs are organized into per-market subfolders so the directory scales cleanly as markets grow. Each .properties file is 13 lines; each xcconfig is ~10 lines — substantive edits touch one file per combo, not a wall of siblings.

**Module split:** features that *read* config depend on `:core:build-config:api` and transitively get only the interface — no BuildKonfig plugin on their classpath, no generated `BuildConfig` object, no `.properties` files merged into their build. Only `composeApp` depends on `:core:build-config:impl` so Metro aggregates the `AppBuildConfigImpl` binding into the production graph. Test modules pull in `:core:build-config:test` for the `@ContributesBinding` `FakeAppBuildConfig`. The `BuildConfigImportTest` Konsist rule enforces the facade boundary: nothing outside `:core:build-config:impl` may import the generated `BuildConfig` object or `AppBuildConfigImpl`.

Per-combo files are plain `.properties` (not `.gradle.kts`). `apply(from = …)` scripts don't inherit the plugin classpath, so the Kotlin DSL form doesn't compile against BuildKonfig's DSL. `.properties` also keeps combos dead-simple: no imports, no logic, just `KEY=value`.

## The AppBuildConfig facade rule

BuildKonfig generates `internal object BuildConfig` in the `:core:build-config:impl` module. **Nothing outside `:core:build-config:impl` may import `BuildConfig` directly.** Consumers go through `AppBuildConfig`, a public **interface**, and receive it via Metro DI — never by static reference.

```kotlin
// Public contract — every consumer depends on this.
interface AppBuildConfig {
    val appName: String
    val market: String
    val env: String
    val baseUrl: String
    val cdnUrl: String
    val locale: String
    val currency: String
}

// Production impl — Metro binds this to AppBuildConfig via ContributesBinding.
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AppBuildConfigImpl : AppBuildConfig {
    override val appName: String = BuildConfig.APP_NAME
    // ...
}
```

The `@ContributesBinding(AppScope::class)` on `AppBuildConfigImpl` registers it as the `AppBuildConfig` binding in `AppScope` — no separate `@Provides` module needed. `core:metro`'s `AppGraph` exposes it as `val appBuildConfig: AppBuildConfig` so composeApp bootstrap can read it from the graph, and any feature's presenter/use case/repo can take `AppBuildConfig` as a constructor parameter and Metro will inject the same singleton.

**Consumers:**
- Presenters, use cases, data sources, repositories: take `AppBuildConfig` as a constructor parameter. Metro injects it automatically.
- Tests: construct a `FakeAppBuildConfig : AppBuildConfig` with whatever values the test needs. Because `AppBuildConfig` is an interface, test doubles are trivial — no reflection, no mocks.
- composeApp bootstrap (`App.kt`, `IosApp.kt`): read from `graph.appBuildConfig`. Never static.

**Never allowed:**
- `import com.mockdonalds.app.core.buildconfig.BuildConfig` outside this module
- `import com.mockdonalds.app.core.buildconfig.AppBuildConfigImpl` anywhere outside this module (it's `internal`, so this is enforced by the compiler)
- Static references like `AppBuildConfig.market` — the interface has no companion; it must be injected.

Why the interface shape: the facade gives us a single place to rename, type-coerce, or doc-comment a field; `internal` keeps generated code internal; and the interface + Metro shape means every consumer is trivially testable with a hand-rolled fake. Konsist enforces coverage against the interface's declared properties.

## Adding a new field

Use the `add-config-field` skill when possible — it does all of this mechanically.

Manually:

1. Add the key to `Defaults.properties` with a safe default.
2. Override per combo in every `markets/*.properties` where it differs. If the field has no sensible default, set it in every combo file and leave `Defaults.properties` blank — the Phase 1 test will catch empty required fields.
3. Add the property to the `AppBuildConfig` **interface** in `AppBuildConfig.kt`.
4. Implement the property in `AppBuildConfigImpl.kt` reading from the generated `BuildConfig` constant.
5. Add an assertion to `AppBuildConfigTest.kt` that references `config.<field>`. The Konsist `BuildConfigCoverageTest` **fails the build** if you skip this step.
6. If the field needs independent injection (e.g. as its own sub-type), introduce a new interface + `@ContributesBinding` impl alongside `AppBuildConfigImpl`. Most fields don't need this — consumers already get the whole `AppBuildConfig` injected.
7. `./gradlew :core:build-config:impl:testAndroidHostTest :testing:architecture-check:test` — both must pass.

## Adding a new market

Each market adds **6 iOS build configurations** (3 envs × 2 build types) and **3 Android property files**. `project.yml` + xcodegen means you never hand-edit `project.pbxproj`.

1. **Properties files.** Create `core/build-config/impl/markets/{market}/` and add all three env files:
   - `{market}-int.properties`
   - `{market}-mte.properties`
   - `{market}-prod.properties`

   Every key in `Defaults.properties` is automatically inherited; override only what differs. `validateAllMarkets` fails the build if any env file is missing — the matrix is symmetry-enforced.

2. **Android.** Nothing else. `applicationId` becomes `com.mockdonalds.app.{market}` automatically via `androidApp/build.gradle.kts`.

3. **iOS xcconfigs.** Create `iosApp/Configuration/{market}/` with all 6 files:
   ```
   {MARKET}-Int-Debug.xcconfig      {MARKET}-Int-Release.xcconfig
   {MARKET}-Mte-Debug.xcconfig      {MARKET}-Mte-Release.xcconfig
   {MARKET}-Prod-Debug.xcconfig     {MARKET}-Prod-Release.xcconfig
   ```

   Use an existing market (e.g. `us/`) as a template. Each file sets `MARKET`, `ENV`, `KOTLIN_FRAMEWORK_BUILD_TYPE`, and build-type-specific optimization flags. `#include "../Base.xcconfig"` pulls in the shared settings.

4. **iOS project.yml.** Add 6 entries to the top-level `configs:` block (Debug → `debug`, Release → `release`) and 6 `configFiles:` entries in each of the three targets (`iosApp`, `iosAppTests`, `iosAppE2ETests`). Pattern is already established — copy an existing market's block and rename.

5. **Regenerate.** `cd iosApp && xcodegen generate`. Commit `project.yml` and the regenerated `project.pbxproj` together. Verify: `xcodebuild -list` shows 6 new configurations and the scheme still defaults correctly.

6. **Smoke build both platforms:**
   ```
   ./gradlew :androidApp:assembleDebug -Pmarket={market} -Penv=int
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iOSApp -configuration {MARKET}-Int-Debug -destination 'generic/platform=iOS Simulator' -sdk iphonesimulator build
   ```

7. **CI matrix.** Add the new market to the market axis.

The `add-market` skill (`.agents/skills/add-market.md`) automates steps 1, 3, 4, and 5 mechanically.

## Adding a new environment

Same shape as a market, but multiplied the other way: 5 new `*-{env}.properties` files (one per market) and 10 new xcconfigs (5 markets × 2 build types). Extend `project.yml` `configs:` and every target's `configFiles:` map, then regenerate. Also update `knownEnvs` in `core/build-config/impl/build.gradle.kts` so `validateAllMarkets` accepts the new env name.

## Enforced rules (Konsist + code review)

1. **Facade coverage** — `BuildConfigCoverageTest` reflects over `AppBuildConfig`'s properties and asserts every one is referenced in `AppBuildConfigTest.kt`. Adding a field without a test fails arch-check.
2. **No direct `BuildConfig` / `AppBuildConfigImpl` imports outside `:core:build-config:impl`** — `BuildConfigImportTest` enforces this. The facade boundary is structural: the api module has no BuildKonfig classpath, so even intra-module code in `:core:build-config:api` cannot reach the generated object.
3. **No feature-flag-shaped field names** (`*Enabled`, `*Flag`, `*Toggle`) — those belong in Harness. (Konsist rule to add when the first violator appears; for now, review-enforced.)
4. **Module must not depend on any feature module.** Enforced by existing core-isolation rules.
5. **AGENTS.md** exists per module — `core/build-config/AGENTS.md` is required and Konsist-enforced.

## Validation rules

These are the rules the `validate-all-markets` skill enforces by parsing every `impl/markets/*.properties` file against `impl/Defaults.properties` — without compiling the module. Cheap enough to run in `verify` and as a CI gate before any market-scoped build.

**Structural rules (parser-only, no domain knowledge required):**

1. **File naming** — every file in `markets/` must match `{market}-{env}.properties` exactly. Lowercase market and env. No other files (no `.DS_Store`, no `README.md`, no orphans).
2. **Defaults exists** — `Defaults.properties` must exist and be non-empty. It is the schema.
3. **Required keys present** — every key in `Defaults.properties` must resolve to a non-empty value in every combo file after merge (combo overrides default; if default is blank, combo must supply). Empty string counts as missing.
4. **No unknown keys** — combo files must not introduce keys absent from `Defaults.properties`. Catches typos (`baseUrl` vs `baseURL`) and dead keys left after a rename.
5. **No duplicate keys within a file** — `.properties` parsers silently take the last value; duplicates almost always indicate a merge mistake.
6. **Every market has every env** — if `us-int.properties` exists, `us-mte.properties` and `us-prod.properties` must also exist. Asymmetric markets fail.

**Format rules (per-field type checks):**

7. **`market`** — 2+ lowercase letters (accepts ISO 3166-1 alpha-2 `us`/`ca`/`de`/`au` and synthetic `core`); must equal the market segment of the filename (`us-int.properties` → `market=us`).
8. **`env`** — must be one of the known envs (`int`, `mte`, `prod`); must equal the env segment of the filename.
9. **`locale`** — BCP 47 form `xx-XX` (`en-US`, `de-DE`). Reject bare `en` or `EN_us`.
10. **`currency`** — exactly 3 uppercase letters (ISO 4217). `USD`, `EUR`, not `usd` or `US$`.
11. **URL fields** (`baseUrl`, `cdnUrl`, any `*Url`) — must parse as an absolute URL with `https://` scheme. No trailing slash. No interpolation tokens (`$market`) — substitution happens at file-write time, not at runtime.
12. **`appName`** — non-empty, no leading/trailing whitespace.
13. **Numeric fields** (e.g. `minimumAge`) — must parse as the declared type; reject `"13 "` or `"thirteen"`.

**What stays Konsist's job (not validate-all-markets):**

- Facade coverage (`AppBuildConfig` interface property → `AppBuildConfigTest` reference) — needs the JVM and reflection (`BuildConfigCoverageTest`)
- No direct `BuildConfig` / `AppBuildConfigImpl` imports outside `:core:build-config:impl` — Kotlin source parsing (`BuildConfigImportTest`)
- No feature-flag-shaped field names — needs Kotlin source parsing
- Module isolation — needs the Gradle dependency graph

The split is deliberate: `validate-all-markets` runs in milliseconds against `.properties` files only, so it can sit in front of every build. Konsist runs against compiled metadata and is heavier; it owns the rules that need a typed view of the code.

**Failure output shape:** the skill aggregates every violation across every file and reports them in one pass — `markets/de-prod.properties: missing required key 'cdnUrl'` / `markets/us-dev.properties: unknown key 'baseURL' (did you mean 'baseUrl'?)`. Never fail-fast on the first error; the whole point is to fix a market in one edit cycle.

## Reference files

- `.agents/standards/markets.md` — cross-cutting market concept: what a market is and how it surfaces across Android, iOS, CI, localization, analytics
- `core/build-config/api/build.gradle.kts` — pure facade module build script
- `core/build-config/impl/build.gradle.kts` — the merge + BuildKonfig wiring; validator config (`knownEnvs`, market/env regexes) lives here
- `core/build-config/test/build.gradle.kts` — test-fixtures module build script
- `core/build-config/AGENTS.md` — module-level summary for agents
- `testing/architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/core/BuildConfigCoverageTest.kt` — the facade coverage rule
- `testing/architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/core/BuildConfigImportTest.kt` — the facade import boundary rule
- `iosApp/project.yml` — **source of truth** for the Xcode project; lists all 30 configs and per-target xcconfig bindings
- `iosApp/Configuration/{market}/*.xcconfig` — iOS combo definitions
- `iosApp/iosApp.xcodeproj/project.pbxproj` — generated by `xcodegen generate`; commit alongside `project.yml` changes but never hand-edit
- `androidApp/build.gradle.kts` — `applicationId` derivation
