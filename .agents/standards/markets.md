# Markets Standard

A **market** is a distinct shippable binary of MockDonalds — one country or sandbox region, with its own bundle identifier, App Store / Play Store listing, backend endpoints, locale, and currency. Markets are the outermost axis of the build matrix; envs and build types sit inside them.

This doc is the cross-cutting reference for the market concept: what it is, which markets exist, and how each one surfaces across every layer of the stack. For the compile-time config schema and validation rules, see `.agents/standards/build-config.md`. For the operational steps to add a new market, see `.agents/skills/add-market/SKILL.md`.

## The market concept

A market is *not*:
- A feature flag or A/B variant (→ Harness)
- A localization bundle (→ Compose MP resources / `.strings` files — orthogonal concern)
- A tenant of a multi-tenant backend (every market hits its own backend hostnames)

A market *is*:
- A row in the `core/build-config/markets/` directory (one subfolder, 3 env files)
- A distinct `applicationId` on Android and `PRODUCT_BUNDLE_IDENTIFIER` on iOS
- A separate store listing (App Store Connect record, Play Console app)
- An axis in CI — every market × env × build-type combo must build cleanly
- A regulatory boundary: currency, legal URLs, data-residency assumptions all follow the market

**Rule:** if the value of something depends on *which country the user downloaded the app in*, it's a market concern. If it depends on *which user has which entitlement*, it's Harness.

## The five markets

| Market | Scope | TLD | Locale | Currency | Status |
|---|---|---|---|---|---|
| `us` | United States | `.com` | `en-US` | `USD` | Real |
| `ca` | Canada | `.ca` | `en-CA` | `CAD` | Real |
| `de` | Germany | `.de` | `de-DE` | `EUR` | Real |
| `au` | Australia | `.com.au` | `en-AU` | `AUD` | Real |
| `core` | Synthetic sandbox (based on `us`) | `.com` with `core-` host prefix | `en-US` | `USD` | Sandbox |

`core` exists so market-neutral work (shared presenters, design-system changes, infra rewiring) can be exercised end-to-end without pointing at a real region's backend. Its URLs resolve to isolated sandbox infrastructure; its applicationId (`com.mockdonalds.app.core`) is not a submittable store identity. Treat it like any other market for code purposes — the validator does — but never ship it.

The market regex in `validateAllMarkets` accepts any 2+ lowercase letters, not strictly ISO 3166-1 alpha-2, precisely to accommodate `core` and any future synthetic variants (`core2`, `staging`, etc.).

## How markets surface across the stack

### Android

`androidApp/build.gradle.kts` declares two AGP `flavorDimensions` — `market` and `env` — so every combo surfaces natively in Android Studio's Build Variants window and as a real Gradle task (`assembleUsIntDebug`, `assembleDeProdRelease`, …). Each market flavor carries its own `applicationIdSuffix`:

```kotlin
android {
    flavorDimensions += listOf("market", "env")
    productFlavors {
        create("us") { dimension = "market"; applicationIdSuffix = ".us" }
        create("ca") { dimension = "market"; applicationIdSuffix = ".ca" }
        create("de") { dimension = "market"; applicationIdSuffix = ".de" }
        create("au") { dimension = "market"; applicationIdSuffix = ".au" }
        create("core") { dimension = "market"; applicationIdSuffix = ".core" }
        create("int") { dimension = "env" }
        create("mte") { dimension = "env" }
        create("prod") { dimension = "env" }
    }
}
```

So selecting `deIntDebug` in the Build Variants window (or running `./gradlew :androidApp:assembleDeProdRelease`) produces `com.mockdonalds.app.de`, a distinct Play Store app. The explicit CLI form `-Pmarket=de -Penv=prod -PbuildType=release` still works and is the path iOS uses. Both paths converge in the shared resolver — `build-logic/convention/src/main/kotlin/com/mockdonalds/buildlogic/BuildVariantResolver.kt` — which `:core:build-config:impl` and `:core:remote-config:impl` call at configure time to pick the right `.properties` files. `versionCode` / `versionName` are currently market-agnostic; if future store submission policy requires per-market versioning, that belongs in `androidApp/build.gradle.kts` next to the flavor declarations.

### iOS

`PRODUCT_BUNDLE_IDENTIFIER` in `iosApp/Configuration/Base.xcconfig` is:

```
PRODUCT_BUNDLE_IDENTIFIER = com.mockdonalds.app.$(MARKET)
```

Each combo xcconfig sets `MARKET = {market}` (lowercase) and `#include "../Base.xcconfig"`, so Xcode substitutes it into the bundle ID at build time. That means every iOS market also produces a distinct App Store Connect record.

The build configuration *name* carries the market in UpperCamel form (`DE-Int-Debug`, `CORE-Prod-Release`). That's cosmetic — the authoritative market value is the `MARKET` setting inside the xcconfig, and that value must be lowercase to match what Gradle receives when the Gradle build phase forwards `-Pmarket=$MARKET`.

### Shared Kotlin (composeApp, features)

Consumers receive the market as a field on `AppBuildConfig`, injected via Metro:

```kotlin
class SomePresenter(private val appBuildConfig: AppBuildConfig) {
    val currentMarket = appBuildConfig.market   // "us", "de", …
    val currentCurrency = appBuildConfig.currency
    val currentLocale = appBuildConfig.locale
}
```

Never read the generated `BuildConfig` object directly — the facade rule in `.agents/standards/build-config.md` forbids it. Features that need market-conditional behavior either take the market string and branch, or (better) take a narrower injected value (`CurrencyFormatter`, `SupportUrlProvider`) so the market branching is centralized.

### CI

**Today**, `.github/workflows/smoke.yml` builds one combo per platform — `us`/`int`/`debug` (`:androidApp:assembleUsIntDebug` on Linux, the `US-Int-Debug` Xcode configuration on macOS) — plus Konsist, Harmonize, and the host unit tests. It is a smoke gate, not the matrix.

**The target** is the full matrix: markets × envs × build types on every PR, with the market axis in the CI config as the authoritative list, so `add-market` can flag it as a required update. Until that exists, a market that builds locally has no guarantee of staying green across commits — the single-combo gate only proves that the shared code, the module graph, and both platform toolchains still work. Widening the axis is a matter of adding a `strategy.matrix` to the two jobs; the cost is macOS runner minutes, which is why it is not on by default.

### Future: localization

Market and locale are separate concerns. Today every market has exactly one locale, but Canada (`en-CA` + `fr-CA`) and future markets will demand multi-locale support. When that lands:

- **Kotlin shared UI** uses Compose Multiplatform resources (`composeResources/values-{lang}/strings.xml`) — locale is resolved at runtime from the device, not baked per market.
- **iOS-native UI** uses `.strings` / `.stringsdict` files per locale.
- **Android-native UI** uses the standard `res/values-{lang}/strings.xml` structure.
- **Compile-time config stays single-locale per market** — `locale` on `AppBuildConfig` is the *default* the market ships with; the runtime Compose/iOS locale resolver can still override it per device.

This split keeps `core:build-config` simple and moves the complexity to the localization layer, which is where it belongs.

### Future: analytics

When analytics lands, `market` is a required dimension on every event. The event pipeline should read `appBuildConfig.market` at emission time — never hardcode per-market event properties. This keeps a market addition a zero-cost change for the analytics layer.

### Future: store listings

App Store Connect and Play Console records are managed outside this repo. The `add-market` skill explicitly calls this out as out-of-scope: the market config defines the binary identity (bundle ID, applicationId); the store listing metadata (screenshots, description, age rating) is submitted via the respective console. Coordinate with the release team before enabling a new market's CI job — a green build of a market without a provisioned store record is a trap.

### Future: backend provisioning

The URLs in `markets/{market}/*.properties` must point at infrastructure that already exists. Adding `markets/jp/` without the backend team having provisioned `int-api.mockdonalds.jp` produces a binary that builds fine and 404s at runtime. The `add-market` skill flags this; PRs that add a market should include confirmation that the endpoints resolve.

## Asymmetry is forbidden

Every market must have every environment. Every env must have every build type (iOS). Every target in `project.yml` must map every configuration. The validator (`validateAllMarkets`) enforces the `.properties` side; Xcode's config inheritance catches most of the `project.yml` side; the rest is code review.

The reason asymmetry is a hard rule: the matrix is the point. If `jp-mte.properties` doesn't exist, QA can't sign off on Japan on the same path they use for every other market. If `iosAppE2ETests` is missing a market's configFiles entry, Xcode silently falls back to a synthesized config and the E2E suite for that market drifts from the app target's settings — you'll find out about it when a release build fails three weeks later.

**Symmetry is cheaper than bespoke.** Prefer "every market has field X, some defaulted to empty" over "only markets A and B have X." The latter turns into lookup-table branching in presenters; the former turns into a single `if (appBuildConfig.x.isNotBlank())` check.

## Adding or removing a market

See `.agents/skills/add-market/SKILL.md` for the end-to-end recipe. The skill covers:

- Creating the 3 `.properties` files
- Creating the 6 xcconfigs
- Editing `project.yml` (30 new lines across 3 targets plus 6 new top-level configs)
- Regenerating with xcodegen
- Smoke-building both platforms
- Flagging the CI matrix update

**Removing a market** is the same in reverse, plus: confirm with the release team that no live store listing depends on the binary, archive the final version submission before deleting, then delete the `markets/{market}/` folder, the `iosApp/Configuration/{market}/` folder, and all `project.yml` entries referencing it. Regenerate. `validateAllMarkets` will pass — it only enforces internal consistency, not historical presence.

## Related references

- `.agents/standards/build-config.md` — compile-time config schema, facade rule, validation rules, `.properties` format
- `.agents/skills/add-market/SKILL.md` — operational recipe to add a new market end-to-end
- `.agents/skills/validate-all-markets/SKILL.md` — the validator itself
- `iosApp/AGENTS.md` → "Project generation (xcodegen)" — how the iOS side of a market materializes into the Xcode project
- `core/build-config/AGENTS.md` — module summary
- `androidApp/build.gradle.kts` — `applicationId` derivation
