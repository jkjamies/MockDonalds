---
name: add-market
description: Scaffold a new market across both platforms — 3 .properties files in core/build-config/markets/{market}/, a new AGP market flavor in androidApp/build.gradle.kts, a single regex extension in build-logic/convention/.../BuildVariantResolver.kt, 6 xcconfigs in iosApp/Configuration/{market}/, YAML edits to iosApp/project.yml for 30 new target-config bindings, xcodegen regeneration, and cross-platform smoke builds. Use when launching the app in a new country/region or adding a synthetic sandbox market.
---

# Add Market

Add a new market end-to-end. Touches both platforms symmetrically; the build (and xcodegen regeneration) is not complete until the matrix is symmetric.

**Parameters:**
- `market` (lowercase, 2+ letters, e.g. `jp`, `mx`, `uk`, `core2`) — required. Accepts ISO 3166-1 alpha-2 and synthetic names.
- `locale` (BCP 47, e.g. `ja-JP`, `es-MX`) — required.
- `currency` (ISO 4217 uppercase, e.g. `JPY`, `MXN`) — required.
- `baseUrlTld` (e.g. `.jp`, `.com.mx`, `.co.uk`) — required. Used to derive per-env URLs.

## Reference standards

- Market concept (cross-cutting): `.agents/standards/markets.md`
- Full spec: `.agents/standards/build-config.md` → "Adding a new market"
- Module summary: `core/build-config/AGENTS.md`
- iOS project generation: `iosApp/AGENTS.md` → "Project generation (xcodegen)"
- Validator rules: `.agents/standards/build-config.md` → "Validation rules"

## Prerequisites

- **xcodegen installed.** `xcodegen --version` should report v2.45+ (`brew install xcodegen` if missing).
- **Existing market as template.** The `us` market is the canonical template — copy from there.

## Steps

### 1. Confirm inputs

Ask the user for any missing parameters. Don't proceed with placeholders. Explicitly confirm:
- Market code (`{market}`)
- Locale (`{LOCALE}`)
- Currency (`{CURRENCY}`)
- TLD for URLs (`{TLD}`, e.g. `.jp`)
- Is this a "real" market or a synthetic sandbox like `core`? If sandbox, URLs use the sandbox pattern (e.g. `{market}-int-api.mockdonalds.com`); see `markets/core/*.properties` for the pattern.

Uppercase variants needed downstream: `{MARKET_UPPER} = market.toUpperCase()` (e.g. `JP`). For the xcconfig files: `{MARKET_UPPER}` in filenames but `MARKET={market}` (lowercase) in file contents — the validator regex requires lowercase in the value.

### 2. Create `core/build-config/impl/markets/{market}/` and three .properties files

For a real market (URL pattern: `{env-prefix}-{service}-api.mockdonalds{tld}`):

**`{market}-int.properties`:**
```
APP_ID={market}-mockdonalds-mobile-int
MARKET={market}
ENV=int
BASE_URL=https://int-api.mockdonalds{TLD}
CDN_URL=https://int-cdn.mockdonalds{TLD}
MENU_BASE_URL=https://int-menu-api.mockdonalds{TLD}
ORDER_BASE_URL=https://int-order-api.mockdonalds{TLD}
ACCOUNT_BASE_URL=https://int-account-api.mockdonalds{TLD}
REWARDS_BASE_URL=https://int-rewards-api.mockdonalds{TLD}
STORE_BASE_URL=https://int-stores-api.mockdonalds{TLD}
LOCALE={LOCALE}
CURRENCY={CURRENCY}
```

**`{market}-mte.properties`:** same as above with `int` → `mte` everywhere.

**`{market}-prod.properties`:** same, but **drop the env prefix from URLs and APP_ID**:
```
APP_ID={market}-mockdonalds-mobile
MARKET={market}
ENV=prod
BASE_URL=https://api.mockdonalds{TLD}
CDN_URL=https://cdn.mockdonalds{TLD}
MENU_BASE_URL=https://menu-api.mockdonalds{TLD}
...
LOCALE={LOCALE}
CURRENCY={CURRENCY}
```

For a synthetic sandbox market: use `{market}-int-{service}.mockdonalds.com` pattern (always `.com` TLD, never a real country suffix). Copy from `impl/markets/core/core-int.properties` as the template.

**Validate immediately:**
```bash
./gradlew :core:build-config:impl:validateAllMarkets
```

Must pass before continuing. If it fails, fix the combo files — symmetry is not optional.

### 3. Register the Android flavor and extend the shared resolver

Two small edits make the new market selectable from Android Studio's Build Variants window and let `:core:build-config:impl` / `:core:feature-flag:impl` resolve it from AGP variant task names.

**a. `androidApp/build.gradle.kts`** — add one market flavor inside `productFlavors { … }`:

```kotlin
create("{market}") { dimension = "market"; applicationIdSuffix = ".{market}" }
```

AGP will now expose 6 new rows (`{market}IntDebug`, `{market}IntRelease`, `{market}MteDebug`, `{market}MteRelease`, `{market}ProdDebug`, `{market}ProdRelease`) and `applicationId` resolves to `com.mockdonalds.app.{market}` automatically.

**b. `build-logic/convention/src/main/kotlin/com/mockdonalds/buildlogic/BuildVariantResolver.kt`** — extend the market alternation in `variantRe`:

```kotlin
private val variantRe = Regex("""(?i)(us|ca|de|au|core|{market})(Int|Mte|Prod)(Debug|Release)""")
```

This is the single place the market list lives. Both `:core:build-config:impl` and `:core:feature-flag:impl` call `BuildVariantResolver.market(project)` / `.env(project)` / `.buildType(project)` — no other `build.gradle.kts` needs editing.

Smoke-verify at configure time:

```bash
./gradlew :androidApp:assemble{Market}IntDebug --dry-run 2>&1 | grep "core:build-config:impl →"
# expected: core:build-config:impl → market={market} env=int buildType=debug
```

### 4. Create `iosApp/Configuration/{market}/` and six xcconfig files

Copy the `us/` templates:

```bash
mkdir -p iosApp/Configuration/{market}
for env in Int Mte Prod; do
  for buildType in Debug Release; do
    cp iosApp/Configuration/us/US-$env-$buildType.xcconfig \
       iosApp/Configuration/{market}/{MARKET_UPPER}-$env-$buildType.xcconfig
  done
done
```

Then edit each of the 6 files to replace the market line:
- `MARKET = us` → `MARKET = {market}`
- Leave `ENV = {int|mte|prod}` untouched — those carry over correctly.
- Leave `KOTLIN_FRAMEWORK_BUILD_TYPE = {debug|release}` untouched.
- Leave optimization flags untouched — they're per-build-type, not per-market.

### 4. Edit `iosApp/project.yml`

Three edits in the same file:

**a. Top-level `configs:` block** — append 6 entries (lowercase `debug`/`release`):

```yaml
  {MARKET_UPPER}-Int-Debug: debug
  {MARKET_UPPER}-Int-Release: release
  {MARKET_UPPER}-Mte-Debug: debug
  {MARKET_UPPER}-Mte-Release: release
  {MARKET_UPPER}-Prod-Debug: debug
  {MARKET_UPPER}-Prod-Release: release
```

**b. `targets.iosApp.configFiles`** — append 6 entries:

```yaml
      {MARKET_UPPER}-Int-Debug: Configuration/{market}/{MARKET_UPPER}-Int-Debug.xcconfig
      {MARKET_UPPER}-Int-Release: Configuration/{market}/{MARKET_UPPER}-Int-Release.xcconfig
      {MARKET_UPPER}-Mte-Debug: Configuration/{market}/{MARKET_UPPER}-Mte-Debug.xcconfig
      {MARKET_UPPER}-Mte-Release: Configuration/{market}/{MARKET_UPPER}-Mte-Release.xcconfig
      {MARKET_UPPER}-Prod-Debug: Configuration/{market}/{MARKET_UPPER}-Prod-Debug.xcconfig
      {MARKET_UPPER}-Prod-Release: Configuration/{market}/{MARKET_UPPER}-Prod-Release.xcconfig
```

**c. Repeat (b) for `targets.iosAppTests.configFiles` and `targets.iosAppE2ETests.configFiles`.** All three targets must map every config. Missing one means Xcode will synthesize a default config for that target, which will drift from the others.

### 5. Regenerate the Xcode project

```bash
cd iosApp && xcodegen generate
```

Takes <1s. Verify:

```bash
xcodebuild -list
```

Should now show **30 + 6 × (the number of new markets) = 36 (for one new market)** build configurations, and one scheme (`iOSApp`).

### 6. Smoke-build both platforms

The matrix is too big to build every combo; pick representative configurations:

```bash
# Android: debug + int (the most common dev path — AGP variant task name drives the resolver)
./gradlew :androidApp:assemble{Market}IntDebug

# Android: release + prod (the minify/obfuscation path)
./gradlew :androidApp:assemble{Market}ProdRelease

# iOS: one debug + one release
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iOSApp \
  -configuration {MARKET_UPPER}-Int-Debug \
  -destination 'generic/platform=iOS Simulator' -sdk iphonesimulator build

xcodebuild -project iosApp/iosApp.xcodeproj -scheme iOSApp \
  -configuration {MARKET_UPPER}-Prod-Release \
  -destination 'generic/platform=iOS Simulator' -sdk iphonesimulator build
```

All four must succeed. If only one build type or one env fails, the issue is isolated there — investigate before claiming completion.

### 7. Update CI matrix

If CI has a market axis (check `.github/workflows/`, `.circleci/config.yml`, etc.), add `{market}` to it.

### 8. Report

Summarize to the user:

- Market code, locale, currency, TLD
- 3 `.properties` files created (paths)
- 6 xcconfigs created (paths)
- `project.yml` diff summary (6 new configs + 18 new configFiles entries across 3 targets = 24 new lines)
- `xcodebuild -list` confirms new configurations visible
- 4 smoke builds passed (or which failed and why)
- CI matrix updated (or flagged for manual update)

## Out of scope

- **Localized strings / resources.** This skill adds the *config* binary for a market, not translated content. String tables, date/number formatting, and copy stay with whatever localization system the project adopts (Compose Multiplatform resources for KMP shared strings, `.strings`/`.stringsdict` for iOS-native, XML for Android-native).
- **App Store / Play Store listings.** Market config defines the binary identity (bundle ID, app ID); the store listings themselves are submitted via App Store Connect / Play Console and aren't part of this repo.
- **Backend provisioning.** The URLs in `.properties` must point at infrastructure that already exists. If the backend team hasn't provisioned `int-api.mockdonalds.jp`, the market is not ready — verify DNS / backend readiness before merging.
- **Market-specific feature toggles.** Compile-time config should not carry `*Enabled` flags. If a feature is gated per-market, that belongs in Harness (runtime), not here.

## Related

- `add-config-field` — add a single new field across the existing matrix (orthogonal axis)
- `validate-all-markets` — pre-flight + post-edit validation of every combo
- `verify` — full pipeline check including market validation
