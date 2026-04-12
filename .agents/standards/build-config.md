# Build Config Standard

Compile-time market + environment configuration lives in `core:build-config` and is emitted via the BuildKonfig Gradle plugin. This doc is the source of truth for how the module is structured, how to add a field, and how enforcement works.

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
| `-Penv`    | `dev` | `-Penv=prod` |

Android: `./gradlew :androidApp:assembleRelease -Pmarket=de -Penv=prod`. `applicationId` is derived in `androidApp/build.gradle.kts` as `com.mockdonalds.app.$market`, so every market produces a distinct Play Store app.

iOS: each combo has its own `.xcconfig` in `iosApp/Configuration/` setting `MARKET`, `ENV`, and `KOTLIN_FRAMEWORK_BUILD_TYPE`. The iosApp target has one build configuration per combo (`US-Dev`, `US-Prod`, `DE-Dev`, `DE-Prod`) backed by those xcconfigs. The shared `iOSApp.xcscheme` defaults Run/Test to `US-Dev` and Archive to `US-Prod`. The Gradle build phase reads `$MARKET` / `$ENV` from the active xcconfig and forwards them via `-Pmarket=` / `-Penv=`. `PRODUCT_BUNDLE_IDENTIFIER` in `Base.xcconfig` is `com.mockdonalds.app.$(MARKET)` so every market gets a distinct App Store listing.

**Switching locally (iOS):** `Product → Scheme → Edit Scheme → Run → Build Configuration`.

## Layout

```
core/build-config/
  build.gradle.kts             applies BuildKonfig, merges Defaults + combo, emits BuildConfig
  Defaults.properties          shared defaults, every field MUST have an entry here
  markets/
    us-dev.properties          one file per market+env
    us-prod.properties
    de-dev.properties
    de-prod.properties
  src/commonMain/kotlin/.../AppBuildConfig.kt       public facade — the ONLY consumer surface
  src/commonTest/kotlin/.../AppBuildConfigTest.kt   Phase 1 smoke test, must reference every field
```

Per-combo files are plain `.properties` (not `.gradle.kts`). `apply(from = …)` scripts don't inherit the plugin classpath, so the Kotlin DSL form doesn't compile against BuildKonfig's DSL. `.properties` also keeps combos dead-simple: no imports, no logic, just `KEY=value`.

## The AppBuildConfig facade rule

BuildKonfig generates `internal object BuildConfig` in this module. **Nothing outside `core:build-config` may import `BuildConfig` directly.** Consumers go through `AppBuildConfig`, a public **interface**, and receive it via Metro DI — never by static reference.

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
7. `./gradlew :core:build-config:testAndroidHostTest :testing:architecture-check:test` — both must pass.

## Adding a new market

1. Create `markets/{market}-dev.properties` and `markets/{market}-prod.properties` (and `stg` when Phase 2 lands).
2. Every key in `Defaults.properties` is automatically inherited; override only what differs.
3. Verify: `./gradlew :composeApp:assembleDebug -Pmarket={market} -Penv=dev`.
4. Android: `applicationId` becomes `com.mockdonalds.app.{market}` automatically.
5. iOS: add `{Market}-Dev.xcconfig` and `{Market}-Prod.xcconfig` plus matching build configurations and scheme entries. At >15 combos move to xcodegen (`project.yml`).
6. Add the market to the CI matrix.

## Adding a new environment

Same shape as adding a market, but new `*-{env}.properties` files for every existing market plus new xcconfigs for every market.

## Enforced rules (Konsist + code review)

1. **Facade coverage** — `BuildConfigCoverageTest` reflects over `AppBuildConfig`'s properties and asserts every one is referenced in `AppBuildConfigTest.kt`. Adding a field without a test fails arch-check.
2. **No direct `BuildConfig` imports outside `core:build-config`** (to be enforced — currently convention; add a Konsist rule if this starts slipping).
3. **No feature-flag-shaped field names** (`*Enabled`, `*Flag`, `*Toggle`) — those belong in Harness. (Konsist rule to add when the first violator appears; for now, review-enforced.)
4. **Module must not depend on any feature module.** Enforced by existing core-isolation rules.
5. **AGENTS.md** exists per module — `core:build-config/AGENTS.md` is required and Konsist-enforced.

## Validation rules

These are the rules the `validate-all-markets` skill enforces by parsing every `markets/*.properties` file against `Defaults.properties` — without compiling the module. Cheap enough to run in `verify` and as a CI gate before any market-scoped build.

**Structural rules (parser-only, no domain knowledge required):**

1. **File naming** — every file in `markets/` must match `{market}-{env}.properties` exactly. Lowercase market and env. No other files (no `.DS_Store`, no `README.md`, no orphans).
2. **Defaults exists** — `Defaults.properties` must exist and be non-empty. It is the schema.
3. **Required keys present** — every key in `Defaults.properties` must resolve to a non-empty value in every combo file after merge (combo overrides default; if default is blank, combo must supply). Empty string counts as missing.
4. **No unknown keys** — combo files must not introduce keys absent from `Defaults.properties`. Catches typos (`baseUrl` vs `baseURL`) and dead keys left after a rename.
5. **No duplicate keys within a file** — `.properties` parsers silently take the last value; duplicates almost always indicate a merge mistake.
6. **Every market has every env** — if `us-dev.properties` exists, `us-prod.properties` must also exist (and `us-stg` once Phase 2 lands). Asymmetric markets fail.

**Format rules (per-field type checks):**

7. **`market`** — 2-letter lowercase ISO 3166-1 alpha-2; must equal the market segment of the filename (`us-dev.properties` → `market=us`).
8. **`env`** — must be one of the known envs (`dev`, `stg`, `prod`); must equal the env segment of the filename.
9. **`locale`** — BCP 47 form `xx-XX` (`en-US`, `de-DE`). Reject bare `en` or `EN_us`.
10. **`currency`** — exactly 3 uppercase letters (ISO 4217). `USD`, `EUR`, not `usd` or `US$`.
11. **URL fields** (`baseUrl`, `cdnUrl`, any `*Url`) — must parse as an absolute URL with `https://` scheme. No trailing slash. No interpolation tokens (`$market`) — substitution happens at file-write time, not at runtime.
12. **`appName`** — non-empty, no leading/trailing whitespace.
13. **Numeric fields** (e.g. `minimumAge`) — must parse as the declared type; reject `"13 "` or `"thirteen"`.

**What stays Konsist's job (not validate-all-markets):**

- Facade coverage (`AppBuildConfig` interface property → `AppBuildConfigTest` reference) — needs the JVM and reflection
- No direct `BuildConfig` imports outside `core:build-config` — needs Kotlin source parsing
- No feature-flag-shaped field names — needs Kotlin source parsing
- Module isolation — needs the Gradle dependency graph

The split is deliberate: `validate-all-markets` runs in milliseconds against `.properties` files only, so it can sit in front of every build. Konsist runs against compiled metadata and is heavier; it owns the rules that need a typed view of the code.

**Failure output shape:** the skill aggregates every violation across every file and reports them in one pass — `markets/de-prod.properties: missing required key 'cdnUrl'` / `markets/us-dev.properties: unknown key 'baseURL' (did you mean 'baseUrl'?)`. Never fail-fast on the first error; the whole point is to fix a market in one edit cycle.

## Reference files

- `core/build-config/build.gradle.kts` — the merge + BuildKonfig wiring
- `core/build-config/AGENTS.md` — module-level summary for agents
- `testing/architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/core/BuildConfigCoverageTest.kt` — the coverage rule
- `iosApp/Configuration/*.xcconfig` — iOS combo definitions
- `androidApp/build.gradle.kts` — `applicationId` derivation
