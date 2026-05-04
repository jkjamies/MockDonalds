# Spec — Kiosk App (`kioskApp` + `kioskComposeApp` + `features/kiosk/*`)

> **Spec type**: `new` (no existing kiosk surface)
> **Downstream skills**: `/add-config-field` (for `appType`) → `/add-feature` × 3 (attract, identify, order) — kiosk skeleton modules created manually because no `/add-app` skill exists
> **Status**: ready for implementation

---

## Overview

The MockDonalds kiosk app is a **second Android-only application target** that runs on in-restaurant self-order terminals (vertical 32" touch-screen hardware, similar to the McDonald's kiosks pictured in the source images). It shares the entire `core/*` infrastructure with the consumer app (Circuit, Metro, CenterPost, auth, network, persistence, analytics, remote-config, build-config, theme, logger, strings, presentation, test-fixtures) and reuses the `features/order` menu domain layer, while adding three kiosk-specific feature modules and its own thin Compose host.

The kiosk runs three screens in sequence:

1. **Attract** — full-bleed rotating ad carousel with a "Touch to Order" overlay. (Image 1, Image 2.)
2. **Identify** — phone-number keypad + (later) QR-code scanner + "Continue without account" skip CTA. (Image 2, bottom strip.)
3. **Order** — vertical category `NavigationRail` + multi-column item grid + bottom cart bar. (Image 3, Image 4.)

After a configurable idle timeout, the kiosk resets to Attract and discards in-flight cart state.

**Initiative name**: `kiosk`
**Primary screens**: `AttractScreen`, `IdentifyScreen`, `KioskOrderScreen`
**App module**: `kioskApp` (AGP application, Android-only) → `kioskComposeApp` (KMP library, Android target only initially)

---

## Business Context

QSR self-order kiosks are an established in-restaurant pattern. The kiosk app shares its menu data, build infrastructure, market matrix, auth backend, analytics SDK, and remote-config provider with the consumer mobile app — but its UX, navigation graph, hardware constraints (portrait-locked, always-on, no system bars, lock-task-mode in production fleet), and lifecycle (idle reset, session-bounded cart, no deep links) are fundamentally different. A separate AGP application target is the right boundary; sharing happens at `core/*` and at the menu-domain layer of `features/order`.

**User stories**:
- As an in-restaurant guest, I want to walk up to a kiosk and start ordering by touching the screen, so I can self-serve without waiting in line.
- As an identified loyalty member, I want to scan my QR code or enter my phone number to associate my order with my account and earn points.
- As an unidentified guest, I want to skip identification and order anonymously, so I don't have to authenticate to use the kiosk.
- As a restaurant operator, I want the kiosk to reset to attract mode after a configurable idle period so abandoned sessions don't leak previous cart state to the next guest.

**Initiative-level acceptance criteria**:
- [ ] A new AGP application module `kioskApp` builds for all 30 variants (5 markets × 3 envs × debug/release/benchmark). `applicationId` base is `com.mockdonalds.kiosk` with the same per-market `.us`/`.ca`/`.de`/`.au`/`.core` suffix as the consumer app.
- [ ] `kioskApp` is the only AGP application module that pulls in `features/kiosk/**`. `androidApp`/`composeApp` do not — enforced by Konsist.
- [ ] `kioskComposeApp` does not pull in any consumer-only feature module (home, more, rewards, profile, recents, scan, login, debug-menu, nutrition) — enforced by Konsist.
- [ ] Kiosk launches into `AttractScreen` (the navigation-graph root). Tapping the screen navigates to `IdentifyScreen`. Successful identify (phone, QR, or skip) navigates to `KioskOrderScreen`.
- [ ] Idle timeout (configurable per screen via remote-config) resets the back stack to `AttractScreen` and clears any pending cart state.
- [ ] All new kiosk feature modules conform to the project's enforced 6-module split and pass the existing Konsist suite plus the new kiosk-specific Konsist rules.
- [ ] `core:build-config` exposes a new `appType: AppType` field (`Consumer` / `Kiosk`); each AGP application sets its own value via BuildKonfig.
- [ ] Kiosk has its own SQLDelight `AppDatabase` aggregator inside `kioskComposeApp` (separate from the consumer's `AppDatabase`).
- [ ] No iOS work in scope — `kioskComposeApp` declares only the Android target initially.

---

## Architecture

### Module graph

```
androidApp (existing AGP application — unchanged)
  └─▶ composeApp (KMP, consumer-only after this change)
        ├─▶ features/{home, login, more, nutrition, order, profile,
        │              recents, rewards, scan, debug-menu}/* (auto-discovered)
        └─▶ core:* (everything)

kioskApp (NEW — AGP application, Android-only)
  └─▶ kioskComposeApp (NEW — KMP library, Android target only)
        ├─▶ features/order/{api/domain, api/navigation,
        │                   impl/data, impl/domain}      ← reused menu domain
        ├─▶ features/kiosk/attract/*                     ← NEW (auto-discovered from features/kiosk/)
        ├─▶ features/kiosk/identify/*                    ← NEW
        ├─▶ features/kiosk/order/*                       ← NEW
        └─▶ core:* (everything)
```

### Why two separate hosts (not one shared `composeApp`)

`composeApp` already auto-discovers every directory under `features/` and depends on it at compile time. If kiosk reused `composeApp` directly:

- The consumer APK would compile against (and at debug time, ship with) every kiosk feature class. R8 strips at release, but the compile-time dependency graph would still mix the two surfaces.
- The kiosk APK would compile against (and at debug time, ship with) every consumer-only feature.
- Konsist boundary rules ("kiosk doesn't depend on consumer-only features") couldn't be enforced at the module-graph level — only via package-import bans, which is a weaker guarantee.

A peer `kioskComposeApp` library keeps dependency graphs disjoint at compile time. The cost is one extra module; the benefit is clean static enforcement.

### What each kiosk module owns

**`kioskApp`** — AGP application, Android-only. Thin shell mirroring `androidApp`:
- `KioskApplication.kt` (`android.app.Application`).
- `KioskMainActivity.kt` — `ComponentActivity` that calls `MockDonaldsKioskApp(application)`. Configures portrait lock, immersive system-bar hiding, `FLAG_KEEP_SCREEN_ON`. No deep-link intent-filter.
- `AndroidManifest.xml` — launcher intent-filter, `screenOrientation="portrait"`, `resizeableActivity="false"`. No camera permission v1 — the QR-scanner half of the identify screen is a simulated placeholder; manifest gets the permission added when CameraX integration ships.
- `res/` — kiosk launcher icons, `Theme.MockDonalds.Kiosk` theme XML, kiosk-specific strings (English-only v1: `kiosk_app_name`).
- `build.gradle.kts` — AGP application config with the same flavor matrix as `androidApp` (5 markets × 3 envs × debug/release/benchmark), but with a different `applicationId` base (`com.mockdonalds.kiosk`). Sets `APP_TYPE=Kiosk` for BuildKonfig.

**`kioskComposeApp`** — KMP library, Android target only initially:
- `commonMain/`:
  - `KioskAppGraphContract.kt` — kiosk-specific surface declared on top of `core:metro.AppGraph` (currently just `kioskIdleTimer`).
  - `KioskIdleTimer.kt` — coroutine-driven idle reset coordinator using `CenterPostDispatchers`.
  - `navigation/KioskAnalyticsListener.kt` — minimal screen-view tracker (mirrors `composeApp`'s `AnalyticsNavigationListener` but without the `InterceptingNavigator` plumbing kiosk doesn't need).
- `androidMain/`:
  - `MockDonaldsKioskApp.kt` — `@Composable` root: `MockDonaldsTheme` → bare `rememberCircuitNavigator` (no `InterceptingNavigator`, no `AuthInterceptor`, no `DeepLinkParser`, no bottom bar) + idle-reset effect + `NavigableCircuitContent`.
  - `ProdKioskAppGraph.kt` — `@DependencyGraph(AppScope::class) interface ProdKioskAppGraph : AppGraph { val kioskIdleTimer: KioskIdleTimer; @Factory fun create(@Provides application: Application): ProdKioskAppGraph }`.
- `commonMain/sqldelight/` — kiosk's own `AppDatabase` (separate from consumer's; kiosk runs in a separate Android process anyway).
- `build.gradle.kts` — explicit `core:*` deps + auto-discovers `features/kiosk/*` + explicit `features/order/{api,impl/data,impl/domain}` reuse.

**`features/kiosk/attract/`** — full 6-module feature for the ad-loop attract screen (see Per-Feature Specs below).

**`features/kiosk/identify/`** — full 6-module feature for phone-number/QR/skip identification.

**`features/kiosk/order/`** — full 6-module feature for the kiosk order screen. Reuses `features/order/api/domain.GetOrderContent`; adds richer fields to `OrderContent` only if needed.

---

## Build Infrastructure Changes

### `core:build-config` — new `appType` and `phoneCountryDialCode` fields

`appType` follows the existing **`buildType: String` pattern** (the KSP fake processor at `:build-tooling:ksp-fake-app-build-config` supports primitives only — enums would crash the type-empty-default generator). Allowed values: `"Consumer"` / `"Kiosk"`. A small `isKiosk` extension keeps callsites readable, mirroring `isDebug`:

```kotlin
package com.mockdonalds.app.core.buildconfig

interface AppBuildConfig {
    // existing fields …
    @DebugConfigField(Group.Identity)
    val appType: String                    // "Consumer" | "Kiosk"

    @DebugConfigField(Group.Localization)
    val phoneCountryDialCode: String       // e.g. "+1", "+49", "+61"
}

val AppBuildConfig.isKiosk: Boolean get() = appType == "Kiosk"
```

**`appType` resolution** — extend `BuildVariantResolver` to detect kiosk task names:

```kotlin
fun appType(project: Project): String = when {
    project.gradle.startParameter.taskNames.any { it.contains(":kioskApp:") || it.startsWith("kioskApp:") } -> "Kiosk"
    else -> "Consumer"
}
```

`core:build-config:impl/build.gradle.kts` adds the BuildKonfig field alongside `BUILD_TYPE`:

```kotlin
val appType: String = BuildVariantResolver.appType(project)
buildkonfig {
    defaultConfigs {
        // …
        buildConfigField(STRING, "BUILD_TYPE", buildType)
        buildConfigField(STRING, "APP_TYPE", appType)
    }
}
```

**`phoneCountryDialCode`** — per-market field, lives in `.properties` files. `Defaults.properties` sets `PHONE_COUNTRY_DIAL_CODE=+1` (covers US/CA/CORE inheritance). DE and AU markets override:
- `markets/de/de-{int,mte,prod}.properties` → `PHONE_COUNTRY_DIAL_CODE=+49`
- `markets/au/au-{int,mte,prod}.properties` → `PHONE_COUNTRY_DIAL_CODE=+61`

`validateAllMarkets` automatically validates the new key (any key in `Defaults.properties` becomes required across all combos, with the default as fallback — existing validator behavior).

### `settings.gradle.kts`

Add:

```kotlin
include(":kioskApp")
include(":kioskComposeApp")

// Kiosk features: features/kiosk/{name}/{api|impl|test}/...
rootDir.resolve("features/kiosk").listFiles()
    ?.filter { it.isDirectory }
    ?.map { it.name }
    ?.sorted()
    ?.forEach { feature ->
        include(":features:kiosk:$feature:api:domain")
        include(":features:kiosk:$feature:api:navigation")
        include(":features:kiosk:$feature:impl:data")
        include(":features:kiosk:$feature:impl:domain")
        include(":features:kiosk:$feature:impl:presentation")
        include(":features:kiosk:$feature:test")
    }
```

Modify the existing top-level features auto-discovery to skip the kiosk subdirectory:

```kotlin
rootDir.resolve("features").listFiles()
    ?.filter { it.isDirectory && it.name != "kiosk" }   // ← add `&& it.name != "kiosk"`
    ?.map { it.name }
    ?.sorted()
    ?.forEach { feature -> /* existing include block */ }
```

### `composeApp/build.gradle.kts` — exclude kiosk

Two existing `features/*` walks (iOS framework export + `commonMain.dependencies`) gain the same `&& it.name != "kiosk"` filter on the top-level enumeration. **No** import of `features/kiosk/*` modules anywhere in `composeApp`.

### `kioskApp/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.mockdonalds.kiosk"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.mockdonalds.kiosk"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        jniLibs {
            pickFirsts += listOf(
                "lib/arm64-v8a/libakamai.so",
                "lib/armeabi-v7a/libakamai.so",
                "lib/x86/libakamai.so",
                "lib/x86_64/libakamai.so",
            )
        }
    }
}

dependencies {
    implementation(project(":kioskComposeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    // CameraX + ML Kit deferred until features/kiosk/identify wires the QR scanner.
}
```

### `kioskComposeApp/build.gradle.kts`

Mirrors `composeApp` shape but Android-only and with explicit feature deps:

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
    alias(libs.plugins.sqldelight)
}

metro { enableCircuitCodegen.set(true) }

sqldelight {
    databases {
        create("AppDatabase") { packageName.set("com.mockdonalds.kiosk.persistence") }
    }
}

kotlin {
    android {
        namespace = "com.mockdonalds.kiosk.composeapp"
        compileSdk = 36
        minSdk = 26
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            // Kiosk features (auto-discovered)
            rootDir.resolve("features/kiosk").listFiles()
                ?.filter { it.isDirectory }
                ?.sorted()
                ?.forEach { feature ->
                    api(project(":features:kiosk:$feature:api:domain"))
                    api(project(":features:kiosk:$feature:api:navigation"))
                    implementation(project(":features:kiosk:$feature:impl:data"))
                    implementation(project(":features:kiosk:$feature:impl:domain"))
                    api(project(":features:kiosk:$feature:impl:presentation"))
                }

            // Reused consumer menu domain — NOT presentation
            api(project(":features:order:api:domain"))
            api(project(":features:order:api:navigation"))
            implementation(project(":features:order:impl:data"))
            implementation(project(":features:order:impl:domain"))

            // Core
            api(project(":core:circuit"))
            api(project(":core:metro"))
            api(project(":core:remote-config:impl"))
            implementation(project(":core:analytics:impl"))
            implementation(project(":core:auth:impl"))
            implementation(project(":core:centerpost"))
            implementation(project(":core:circuit"))
            implementation(project(":core:theme"))
            implementation(project(":core:network:impl"))
            implementation(project(":core:build-config:impl"))
            implementation(project(":core:persistence:impl"))
            implementation(project(":core:logger:impl"))

            implementation(libs.circuit.foundation)
            implementation(libs.circuit.runtime)
            implementation(libs.circuit.runtime.presenter)
            implementation(libs.circuit.runtime.ui)
            implementation(libs.circuit.retained)
            implementation(libs.metro.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(project(":core:test-fixtures"))
            implementation(libs.circuit.test)
        }
    }
}
```

No `iosX64()` / `iosArm64()` / `iosSimulatorArm64()` targets, no iOS framework export, no `bridge/*` source, no `nativecoroutines` dep. Adding iOS later is purely additive.

### Konsist rules — new tests in `:testing:architecture-check`

```
ConsumerHostExcludesKioskFeatures
  composeApp/** Kotlin files must not import com.mockdonalds.app.features.kiosk*

KioskHostExcludesConsumerOnlyFeatures
  kioskComposeApp/** must not import com.mockdonalds.app.features.{home, more, rewards,
    profile, recents, scan, login, debug-menu, nutrition}*

KioskAppExcludesTabScreens
  kioskApp/** and kioskComposeApp/** must not reference any class implementing
  com.mockdonalds.app.core.circuit.TabScreen

KioskFeaturesExcludeConsumerFeatures
  features/kiosk/**/Kotlin files must not import com.mockdonalds.app.features.{home, more,
    rewards, profile, recents, scan, login, debug-menu, nutrition}*
```

The existing module-shape Konsist tests (6-module split, `api/` purity, etc.) auto-cover `features/kiosk/*` because they walk the module list, not hardcoded names.

---

## Per-Feature Specs

### `features/kiosk/attract`

Reference images: **Image 1** (vertical kiosk with "BACON MEETS OUR CLASSICS / Order Here / Touch to Order / Tocar para Ordenar"), **Image 2** (similar attract loop with "Try a Bacon Cajun Ranch McCrispy / Start Order / Earn Points").

#### Domain Models (api/domain)

```kotlin
// AttractContent.kt
data class AttractContent(
    val ads: List<Ad>,
    val rotationSeconds: Int,
)

data class Ad(
    val id: String,
    val imageUrl: String,
    val headline: String,        // "BACON MEETS OUR CLASSICS"
    val subheadline: String?,    // "Order Here"
)
```

No enums in v1. `Ad` is the unit of content the carousel rotates through.

#### Use Cases

| Name | Type | Params | Result | Description |
|------|------|--------|--------|-------------|
| `GetAttractContent` | `CenterPostSubjectInteractor` | `Unit` | `AttractContent` | Stream the current ad list + rotation cadence. |

#### Repository

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getAttractContent()` | `Flow<AttractContent>` | Combines remote-config-driven ad list with a static fallback. |

**Data sources**: remote-config-driven via `core:remote-config:api`. The fallback (if remote-config returns empty) is a hardcoded list of 3 placeholder ads in `AttractRepositoryImpl` — sufficient for kiosk to look populated on day one without backend support. Production ad delivery in a real fleet would come from a marketing-content platform (Braze or similar) feeding remote-config or a dedicated ad endpoint; the `AttractRepository` interface is the seam for swapping the source without touching presenters.

Static placeholder shape (v1):

```kotlin
listOf(
    Ad(id = "1", imageUrl = "https://example.test/ads/1.png", headline = "MIDNIGHT TRUFFLE NIGHTS", subheadline = "Order Here"),
    Ad(id = "2", imageUrl = "https://example.test/ads/2.png", headline = "SAFFRON FRY FRIDAYS", subheadline = "Order Here"),
    Ad(id = "3", imageUrl = "https://example.test/ads/3.png", headline = "TASTE THE SIGNATURE LINE", subheadline = "Order Here"),
)
```

`example.test` is the RFC2606 reserved domain — won't 404 in dev, won't pretend to be real.

#### Screen & UI

**Screen type**: `Screen` (data object, no params).

```kotlin
@Parcelize
data object AttractScreen : Screen
```

**UI States**:

```kotlin
data class AttractUiState(
    val ads: List<Ad>,
    val currentIndex: Int,
    val rotationSeconds: Int,
    val eventSink: (AttractEvent) -> Unit,
) : CircuitUiState
```

**Events** (sealed class):

```kotlin
sealed class AttractEvent : CircuitUiEvent {
    data object TouchToOrder : AttractEvent()
    data class IndexChanged(val index: Int) : AttractEvent()  // for swipe-driven manual advance
}
```

**UI description (Android Compose)**:

- Full-bleed `Box(Modifier.fillMaxSize())` with `pointerInput(Unit) { detectTapGestures { state.eventSink(TouchToOrder) } }` overlay catching any tap anywhere on the screen.
- Inside: a `HorizontalPager` cycling `state.ads`. `LaunchedEffect(state.currentIndex)` advances every `rotationSeconds` seconds via `CenterPostDispatchers.default`.
- Each page renders the ad's `imageUrl` (Coil `AsyncImage`) full-bleed; overlay text at top center renders `headline` (large, bold, brand-yellow on dark), `subheadline` below it.
- Bottom-center: persistent `Touch to Order` text overlay (English-only v1; bilingual / localization deferred until Phrase wiring is addressed).
- Branding mark (`@drawable/mockdonalds_arches_yellow`) bottom-left, semi-transparent.

**Test tags**:

```kotlin
object AttractTestTags {
    const val SCREEN = "attract_screen"
    const val PAGER = "attract_pager"
    const val TOUCH_TO_ORDER_OVERLAY = "attract_touch_to_order"
    const val AD_IMAGE = "attract_ad_image"  // suffixed by ad id at runtime
}
```

#### Navigation

- **Entry**: navigation-graph root; first screen the kiosk shows on launch (`rememberSaveableBackStack(root = AttractScreen)`).
- **Outgoing**: `TouchToOrder` → `navigator.goTo(IdentifyScreen())`.
- **Idle**: `KioskIdleTimer` is *disabled* on `AttractScreen` (it's already the idle state).

#### Analytics

| Event | Trigger | Properties |
|---|---|---|
| `kiosk.attract.viewed` | Auto via `KioskAnalyticsListener` on `goTo(AttractScreen)` / `resetRoot(AttractScreen)` | — |
| `kiosk.attract.dwell` | When `TouchToOrder` fires | `dwell_ms` (since last reset), `current_ad_id` |

#### Feature Flags

| Flag | Default | Lifecycle | Description |
|---|---|---|---|
| `kiosk.attract.rotation_seconds` | `8` | `Ops` | Override the per-ad rotation interval at runtime. |
| `kiosk.attract.enabled` | `true` | `KillSwitch` | Disabling renders a static "Touch to Order" splash with no ads (for incident response). |

---

### `features/kiosk/identify`

Reference images: **Image 2** (bottom strip: "Start Order" + "Earn Points / Scan now QR" CTA pair). Phase-1 implementation is keypad + skip; QR scanner deferred to a follow-up.

#### Domain Models (api/domain)

```kotlin
data class IdentifyContent(
    val skipEnabled: Boolean,             // remote-config-gated; always true in v1
    val phoneEntryEnabled: Boolean,       // remote-config-gated; always true in v1
    val qrScannerEnabled: Boolean,        // remote-config-gated; true in v1 (renders simulated placeholder; real CameraX deferred)
    val countryDialCode: String,          // "+1" for US, "+1" for CA, "+49" for DE, "+61" for AU
)

sealed class IdentifyResult {
    data class Identified(val accountId: String) : IdentifyResult()
    data object GuestSession : IdentifyResult()
    data class Failed(val reason: String) : IdentifyResult()
}
```

#### Use Cases

| Name | Type | Params | Result | Description |
|------|------|--------|--------|-------------|
| `GetIdentifyContent` | `CenterPostSubjectInteractor` | `Unit` | `IdentifyContent` | Stream which identification methods are enabled. |
| `IdentifyByPhoneNumber` | `CenterPostInteractor` | `String` (E.164 phone) | `IdentifyResult` | Look up account by phone; updates `core:auth.AuthManager`. |
| `IdentifyByQrCode` | `CenterPostInteractor` | `String` (QR payload) | `IdentifyResult` | Decode QR loyalty token; updates `AuthManager`. *Deferred to follow-up — interface defined; impl returns `Failed("not_implemented")`.* |
| `ContinueAsGuest` | `CenterPostInteractor` | `Unit` | `IdentifyResult.GuestSession` | Sets `AuthManager` to a guest session; no network call. |

#### Repository

| Method | Return Type | Description |
|---|---|---|
| `getIdentifyContent()` | `Flow<IdentifyContent>` | Reads remote-config flag set + per-market dial-code from `AppBuildConfig`. |
| `identifyByPhone(phone)` | `suspend Result<IdentifyResult>` | POST `/v1/kiosk/identify` with `{ phone, kioskTerminalId }`. |
| `identifyByQrCode(payload)` | `suspend Result<IdentifyResult>` | POST `/v1/kiosk/identify` with `{ qr, kioskTerminalId }`. *Stubbed v1.* |

**Endpoint** (single endpoint, both methods):

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/v1/kiosk/identify` | none (kiosk identifies the guest, not the device) | Body: `{ "method": "phone"|"qr", "value": "...", "terminal_id": "..." }`. Response: `{ "account_id": "abc-123", "guest_token": "..." }`. |

**Phase-1 simplification**: backend likely doesn't have this endpoint yet. `IdentifyByPhoneNumberImpl` returns `IdentifyResult.Identified(accountId = "kiosk-stub-${phone.takeLast(4)}")` deterministically until the real endpoint exists. Document the stub in the repository AGENTS.md.

#### Screen & UI

**Screen type**: `Screen` (data class with optional `next: Screen?`):

```kotlin
@Parcelize
data class IdentifyScreen(
    val next: Screen? = null,
) : Screen
```

`next` defaults to `KioskOrderScreen`; the parameter exists so future flows (e.g., redeem-offer mid-cart) can route back to a non-default destination.

**UI States**:

```kotlin
data class IdentifyUiState(
    val phoneInput: String,                  // raw digits, formatted in UI
    val isSubmitting: Boolean,
    val errorMessage: String?,
    val skipEnabled: Boolean,
    val qrScannerEnabled: Boolean,
    val countryDialCode: String,
    val eventSink: (IdentifyEvent) -> Unit,
) : CircuitUiState
```

**Events**:

```kotlin
sealed class IdentifyEvent : CircuitUiEvent {
    data class DigitPressed(val digit: Char) : IdentifyEvent()
    data object DeletePressed : IdentifyEvent()
    data object SubmitPhonePressed : IdentifyEvent()
    data class QrCodeDetected(val payload: String) : IdentifyEvent()
    data object SkipPressed : IdentifyEvent()
    data object DismissError : IdentifyEvent()
}
```

**UI description** (Android Compose, portrait kiosk):

- Top header (~10% height): yellow brand bar with "Sign in to earn points" + "Continue without account" subline.
- Body split horizontally:
  - Left half: large simulated `QrScannerPlaceholder` showing a QR icon + "Scan now" text + a faux viewfinder frame. Tappable in v1 dispatches a fake `QrCodeDetected("simulated-payload-${timestamp}")` event so the downstream identify flow can be exercised end-to-end without real hardware. When phase-2 lands, this slot hosts a `CameraX` preview with `MlKitBarcodeAnalyzer` and the simulated tap path goes away.
  - Right half: dial-code prefix (`+1`) + formatted phone display (`(555) 123-4567`) + a 3×4 keypad of large `KeypadButton`s (digits 0-9, delete, submit). Each key is ≥ 80dp tall for kiosk touch.
- Bottom CTA (~12% height): full-width yellow `Button` "Continue without account / Skip". Visible iff `state.skipEnabled`.
- Loading: `state.isSubmitting` overlays a translucent `CircularProgressIndicator` over the body.
- Error: `state.errorMessage != null` shows a toast-style banner above the keypad with a dismiss action; auto-dismisses after 4s.

**Test tags**:

```kotlin
object IdentifyTestTags {
    const val SCREEN = "identify_screen"
    const val PHONE_DISPLAY = "identify_phone_display"
    const val KEYPAD_DIGIT_PREFIX = "identify_keypad_digit_"  // suffixed by digit
    const val DELETE = "identify_delete"
    const val SUBMIT = "identify_submit"
    const val QR_SCANNER = "identify_qr_scanner"
    const val SKIP = "identify_skip"
    const val ERROR_BANNER = "identify_error_banner"
    const val LOADING_OVERLAY = "identify_loading_overlay"
}
```

#### Navigation

- **Entry**: from `AttractScreen` via `TouchToOrder`.
- **Outgoing**:
  - `IdentifyResult.Identified` → `navigator.resetRoot(state.next ?: KioskOrderScreen)` (resetRoot, not goTo, so back gesture doesn't return to Identify).
  - `IdentifyResult.GuestSession` → same.
  - `IdentifyResult.Failed` → stay on screen, show `state.errorMessage`.
- **Idle reset**: `KioskIdleTimer` configured to 60s on this screen; expiry resets to `AttractScreen`.

#### Analytics

| Event | Trigger | Properties |
|---|---|---|
| `kiosk.identify.viewed` | Auto on resetRoot | — |
| `kiosk.identify.phone_submitted` | `SubmitPhonePressed` after validation | `digits_count`, `country_dial_code` |
| `kiosk.identify.skip_pressed` | `SkipPressed` | — |
| `kiosk.identify.qr_detected` | `QrCodeDetected` | `payload_hash` (sha256, never raw) |
| `kiosk.identify.success` | Any `IdentifyResult.Identified` or `GuestSession` | `method` ("phone"|"qr"|"guest") |
| `kiosk.identify.failed` | Any `IdentifyResult.Failed` | `method`, `reason` |

#### Feature Flags

| Flag | Default | Lifecycle | Description |
|---|---|---|---|
| `kiosk.identify.skip_enabled` | `true` | `Ops` | Hide the skip CTA to force identification (per-store policy). |
| `kiosk.identify.qr_scanner_enabled` | `true` | `Permanent` | Renders simulated placeholder v1; flag flip stays `true` when CameraX integration replaces the simulation. |
| `kiosk.identify.idle_timeout_seconds` | `60` | `Ops` | Override per-screen idle timeout. |

---

### `features/kiosk/order`

Reference images: **Image 3** (Burgers grid with vertical sidebar of categories, Big Mac/Hamburger/McDouble/Quarter Pounder grid with calories + price, Back + Scan Offer top-right buttons), **Image 4** (Around the World Menu, 3-col grid, $0.00 / Cart / Pay button at bottom).

#### Domain Models — extend `features/order/api/domain` additively

The existing `OrderContent` shape is too thin for kiosk. Add fields additively (consumer benefits too — current consumer order screen would happily render images and calories if available):

```kotlin
// features/order/api/domain/OrderModels.kt — additions

data class MenuCategory(
    val id: String,
    val name: String,
    val iconUrl: String? = null,    // NEW — sidebar icon (e.g., burger thumbnail in Image 3)
)

data class MenuItem(                 // NEW — the per-category item shape
    val id: String,
    val categoryId: String,
    val name: String,                // "Big Mac"
    val priceFormatted: String,      // "$4.59"
    val calories: Int?,              // 540
    val imageUrl: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),  // e.g., ["new", "spicy"]
)

data class OrderContent(
    val categories: List<MenuCategory>,
    val featuredItems: List<FeaturedItem>,    // existing — consumer order screen still uses this
    val itemsByCategory: Map<String, List<MenuItem>>,  // NEW — keyed by category id
    val cartSummary: CartSummary,
)
```

`features/order/impl/data.OrderRepositoryImpl` populates the new fields with the same fake data it currently exposes (extended to ~5 items per category). The existing consumer `OrderPresenter` ignores the new fields — backwards compatible.

#### Use Cases

Reuse `features/order/api/domain.GetOrderContent` unchanged. No new use case for v1.

#### Screen & UI

**Screen type**: `Screen`:

```kotlin
@Parcelize
data object KioskOrderScreen : Screen
```

**UI States**:

```kotlin
data class KioskOrderUiState(
    val categories: List<MenuCategory>,
    val selectedCategoryId: String?,
    val itemsForSelectedCategory: List<MenuItem>,
    val cartSummary: CartSummary?,
    val eventSink: (KioskOrderEvent) -> Unit,
) : CircuitUiState
```

**Events**:

```kotlin
sealed class KioskOrderEvent : CircuitUiEvent {
    data class CategorySelected(val id: String) : KioskOrderEvent()
    data class ItemTapped(val id: String) : KioskOrderEvent()
    data object ScanOfferPressed : KioskOrderEvent()
    data object BackPressed : KioskOrderEvent()
    data object CartPressed : KioskOrderEvent()
    data object CancelOrderPressed : KioskOrderEvent()
    data object PayPressed : KioskOrderEvent()
}
```

**UI description** (Android Compose, portrait):

```
Row(Modifier.fillMaxSize()) {
    NavigationRail(modifier = Modifier.width(112.dp).fillMaxHeight()) {
        // Top: M-arches "HOME" tile (square yellow tile with white M).
        NavigationRailItem(
            selected = false,
            onClick = { state.eventSink(BackPressed) },
            icon = { Image(painterResource(R.drawable.kiosk_home_arches), null) },
            label = { Text("HOME") },
        )
        state.categories.forEach { category ->
            NavigationRailItem(
                selected = category.id == state.selectedCategoryId,
                onClick = { state.eventSink(CategorySelected(category.id)) },
                icon = { CategoryIcon(category) },
                label = { Text(category.name) },
            )
        }
    }

    Column(Modifier.weight(1f).fillMaxHeight()) {
        TopBar(                                         // "Burgers" / "Around the World Menu" + Back/Scan-Offer
            categoryName = state.selectedCategoryName,
            onBack = { state.eventSink(BackPressed) },
            onScanOffer = { state.eventSink(ScanOfferPressed) },
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),               // 3 columns matches Image 4; tighter to 2 if narrower
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(SpacingMd),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            items(state.itemsForSelectedCategory, key = { it.id }) { item ->
                KioskMenuItemCard(item = item, onClick = { state.eventSink(ItemTapped(item.id)) })
            }
        }

        KioskCartBar(                                   // "$0.00" + Cancel + Pay (Image 4 bottom strip)
            cartSummary = state.cartSummary,
            onCancel = { state.eventSink(CancelOrderPressed) },
            onPay = { state.eventSink(PayPressed) },
        )
    }
}
```

`KioskMenuItemCard` (per Image 3 / Image 4):
- Card with rounded corners and white background.
- Top: `AsyncImage(item.imageUrl)` ~70% of card height.
- Bottom: item name (bold), `priceFormatted`, calorie count if non-null (`"${item.calories} Cal"`), tag chips (e.g., "DELUXE", "NEW").
- Card is large enough for kiosk touch (≥ 200dp wide × 280dp tall in 3-col layout).

`KioskCartBar`:
- Horizontal bar pinned to bottom of right column.
- Left: M-arches loyalty tile + "Scan now" subline (small).
- Center: cart total `cartSummary.total` with item count badge.
- Right: red "Cancel" button + yellow "Pay" button. Disabled if `cartSummary?.itemCount == 0`.

`TopBar`:
- Left: large category name (e.g., "Burgers").
- Right: stacked "Back" + "Scan Offer" buttons (per Image 3).

**Test tags**:

```kotlin
object KioskOrderTestTags {
    const val SCREEN = "kiosk_order_screen"
    const val NAV_RAIL = "kiosk_order_nav_rail"
    const val NAV_RAIL_HOME = "kiosk_order_nav_home"
    const val NAV_RAIL_CATEGORY_PREFIX = "kiosk_order_nav_category_"   // suffix: category id
    const val ITEM_GRID = "kiosk_order_item_grid"
    const val ITEM_CARD_PREFIX = "kiosk_order_item_"                   // suffix: item id
    const val CART_BAR = "kiosk_order_cart_bar"
    const val CART_TOTAL = "kiosk_order_cart_total"
    const val CANCEL_BUTTON = "kiosk_order_cancel"
    const val PAY_BUTTON = "kiosk_order_pay"
    const val SCAN_OFFER_BUTTON = "kiosk_order_scan_offer"
    const val BACK_BUTTON = "kiosk_order_back"
}
```

#### Navigation

- **Entry**: `IdentifyScreen` → `resetRoot(KioskOrderScreen)` after successful identify or skip.
- **Outgoing v1**:
  - `BackPressed` → `navigator.resetRoot(AttractScreen)` (kiosk "back" returns to attract, since there's no parent).
  - `ItemTapped`, `ScanOfferPressed`, `CartPressed`, `CancelOrderPressed`, `PayPressed` → no-op or toast in v1 (downstream screens out of scope).
- **Idle reset**: 120s on this screen; longer than Identify because guests need time to browse.

#### Analytics

| Event | Trigger | Properties |
|---|---|---|
| `kiosk.order.viewed` | Auto on resetRoot | — |
| `kiosk.order.category_selected` | `CategorySelected` | `category_id`, `category_name` |
| `kiosk.order.item_tapped` | `ItemTapped` | `item_id`, `category_id`, `position` |
| `kiosk.order.scan_offer_pressed` | `ScanOfferPressed` | — |
| `kiosk.order.cancel_pressed` | `CancelOrderPressed` | `cart_item_count`, `cart_total` |
| `kiosk.order.pay_pressed` | `PayPressed` | `cart_item_count`, `cart_total` |

#### Feature Flags

| Flag | Default | Lifecycle | Description |
|---|---|---|---|
| `kiosk.order.idle_timeout_seconds` | `120` | `Ops` | Per-screen idle timeout. |
| `kiosk.order.show_calories` | `true` | `Ops` | Per-market hideable (some EU markets require, some optional). |
| `kiosk.order.show_scan_offer` | `true` | `Ops` | Hide the Scan Offer button if loyalty integration isn't live in a market. |

---

## Kiosk Host (`kioskComposeApp/MockDonaldsKioskApp.kt`)

```kotlin
@Composable
fun MockDonaldsKioskApp(application: Application) {
    val graph = remember(application) {
        createGraphFactory<ProdKioskAppGraph.Factory>().create(application).also {
            it.loggerInitializer.initialize()
        }
    }

    MockDonaldsTheme(forceDark = false) {                         // kiosk follows brand light theme
        val backStack = rememberSaveableBackStack(root = AttractScreen)
        val navigator = rememberCircuitNavigator(backStack) { /* root pop = no-op on kiosk */ }
        val analyticsListener = remember { KioskAnalyticsListener(graph.analyticsDispatcher) }

        // Idle reset: any pointer event pokes the timer; expiry resets to AttractScreen.
        IdleResetEffect(
            timer = graph.kioskIdleTimer,
            navigator = navigator,
            attractScreen = AttractScreen,
            currentScreen = backStack.topRecord?.screen,
        )

        // Manual screen-view tracking (no InterceptingNavigator wrapper).
        LaunchedEffect(backStack.topRecord?.screen) {
            backStack.topRecord?.screen?.let { analyticsListener.onScreenShown(it) }
        }

        CircuitCompositionLocals(graph.circuit) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture { graph.kioskIdleTimer.poke() }
                    }
            ) {
                NavigableCircuitContent(navigator = navigator, backStack = backStack)
            }
        }
    }
}
```

### Idle timer (`KioskIdleTimer`)

```kotlin
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class KioskIdleTimer @Inject constructor(
    private val dispatchers: CenterPostDispatchers,
    private val remoteConfig: RemoteConfigRepository,
) {
    private val pokeFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val expiryFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val expirations: SharedFlow<Unit> = expiryFlow.asSharedFlow()

    fun poke() { pokeFlow.tryEmit(Unit) }

    suspend fun runFor(screenKey: String) = withContext(dispatchers.default) {
        val timeout = remoteConfig.numberOrDefault(
            key = "kiosk.${screenKey}.idle_timeout_seconds",
            default = DEFAULT_TIMEOUT_BY_SCREEN.getValue(screenKey),
        ).toLong().seconds
        pokeFlow.timeout(timeout).collect { /* swallow normal pokes */ }
            .also { expiryFlow.emit(Unit) }
    }

    companion object {
        private val DEFAULT_TIMEOUT_BY_SCREEN = mapOf(
            "attract" to 0,        // 0 = disabled
            "identify" to 60,
            "order" to 120,
        )
    }
}
```

`IdleResetEffect` switches the active timer based on `currentScreen`:

```kotlin
@Composable
fun IdleResetEffect(
    timer: KioskIdleTimer,
    navigator: Navigator,
    attractScreen: Screen,
    currentScreen: Screen?,
) {
    val key = when (currentScreen) {
        AttractScreen -> "attract"
        is IdentifyScreen -> "identify"
        KioskOrderScreen -> "order"
        else -> null
    }
    LaunchedEffect(key) {
        if (key == null || key == "attract") return@LaunchedEffect
        launch { timer.runFor(key) }
        timer.expirations.collect {
            if (navigator.peekBackStack().firstOrNull() != attractScreen) {
                navigator.resetRoot(attractScreen)
            }
        }
    }
}
```

(`runFor` and the collect logic are sketched here; concrete implementation may use a single coroutine that flips behavior on key change. The contract is: while non-attract, expiry pokes a `resetRoot(AttractScreen)`.)

---

## Markets, Variants, Build Config

| Aspect | Consumer (`androidApp`) | Kiosk (`kioskApp`) |
|---|---|---|
| `applicationId` base | `com.mockdonalds.app` | `com.mockdonalds.kiosk` |
| Per-market suffix | `.us`/`.ca`/`.de`/`.au`/`.core` | Same |
| Flavor matrix | 5 × 3 × {debug, release, benchmark} = 30 | Same |
| BuildKonfig fields | All existing + new `appType` | Same |
| `BuildVariantResolver` | Already supports task-name parsing | **No change** — same regex matches `:kioskApp:assembleUsIntDebug` etc. |
| `core:strings` | Phrase-fed Android resources | Same — kiosk-specific keys added (`kiosk_attract_touch_to_order`, `kiosk_identify_skip`, `kiosk_order_scan_offer`, etc.) |

---

## Persistence

`kioskComposeApp` is the SQLDelight aggregator for kiosk's `AppDatabase`, separate from the consumer app's. Kiosk-only tables live in `features/kiosk/order/impl/data/src/commonMain/sqldelight/` (e.g., `kiosk_session.sq` for in-progress cart, `kiosk_attract_cache.sq` for ad-list caching).

The two `AppDatabase` instances are physically separate SQLite files in physically separate Android sandboxes (different `applicationId`). No cross-app shared state.

---

## Hardware & Device Concerns

| Concern | Approach |
|---|---|
| Orientation lock | Manifest `android:screenOrientation="portrait"` on `KioskMainActivity`. |
| Always-on display | `window.addFlags(FLAG_KEEP_SCREEN_ON)` in `onCreate`. |
| Immersive mode | `WindowCompat.setDecorFitsSystemWindows(window, false)` + `WindowInsetsControllerCompat.hide(systemBars())` + `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`. |
| Lock-task / kiosk mode | Documented in `kioskApp/AGENTS.md` — production fleet uses DPM `setLockTaskPackages` from a device-owner DPC. **Not auto-configured** in the app; that's deployment infra. |
| Camera (for QR scanner phase-2) | **Not declared v1.** The QR-scanner half of the identify screen renders a simulated placeholder. Manifest permission + runtime request added when CameraX integration ships. |
| Splash | `androidx.core.splashscreen` for the brief OS splash; the "ads splash" the user described is `AttractScreen`, not the system splash. |
| Akamai `.so` dedup | Same `pickFirsts` packaging block as `androidApp`. |

---

## Testing Strategy

Per the project's 5 test levels:

| Level | Coverage |
|---|---|
| **Unit (Kotlin, Kotest BehaviorSpec, JVM)** | `AttractPresenterTest`, `IdentifyPresenterTest`, `KioskOrderPresenterTest`. Repository tests for each feature's `impl/data`. Use-case impl tests (`GetAttractContentImplTest`, `IdentifyByPhoneNumberImplTest`, etc.). `KioskIdleTimerTest`. Each abstract use case ships a Fake in `features/kiosk/{name}/test/`. |
| **UI Component (Compose, Robot pattern)** | `androidDeviceTest/` Robot triplet for each screen: `AttractUiTest`+`AttractUiRobot`+`AttractStateRobot`, same for Identify and KioskOrder. Asserts test tags, state→UI plumbing, button-tap event dispatch. |
| **Architecture (Konsist)** | New rules listed in §Build Infrastructure / Konsist. Existing 6-module-shape rules auto-cover `features/kiosk/*`. |
| **Nav/Int** | New kiosk navint suite in `:testing:navint-tests` covering: (a) `Attract.touch` → Identify presented, (b) `Identify.skip` → KioskOrder presented as root, (c) `Identify.phone_submit` → KioskOrder presented as root, (d) idle expiry on Identify resets to Attract, (e) idle expiry on KioskOrder resets to Attract. |
| **E2E** | New kiosk journey test in `:testing:e2e-tests` driving the full flow against a connected emulator with the kiosk APK installed. |
| **Benchmark** | Cold-start macrobenchmark from launcher → AttractScreen rendered. Warm transition Attract → Identify → KioskOrder. Lives in `:testing:benchmarks` alongside the consumer benchmarks. |

Sentinel data hygiene: tests use placeholder image URLs from `https://example.test/` (RFC2606 reserved) so no real network calls.

---

## Cross-Feature Dependencies

**Imports from**:
- `core:circuit` — `Screen`, `Navigator`, `@Parcelize`, `CircuitProviders` (each kiosk feature's nav module).
- `core:centerpost` — `CenterPostInteractor`, `CenterPostSubjectInteractor`, `CenterPostDispatchers` (auto-wired).
- `core:metro` — `AppGraph` interface (kiosk graph extends it).
- `core:auth:api` — `AuthManager` (identify funnels through it).
- `core:network:impl` — `HttpClientFactory` (identify endpoint client).
- `core:remote-config:impl` — `RemoteConfigRepository` (idle timeouts, attract rotation, feature gates).
- `core:analytics:impl` — `AnalyticsDispatcher`, `TrackAnalyticsEvent` (custom events per feature).
- `core:build-config:api` — `AppBuildConfig` (`appType`, market dial-code).
- `core:persistence:impl` — `DatabaseDriverFactory` (kiosk-owned `AppDatabase`).
- `core:theme` — `MockDonaldsTheme`, `MockDimens`, `MaterialTheme.colorScheme`.
- `core:strings` — kiosk-specific keys (Phrase-fed).
- `features/order/api/domain` + `impl/{data, domain}` — reused menu domain.

**Imported by**:
- `kioskComposeApp` only. **No cross-imports between consumer composeApp and any kiosk feature.**

---

## Phased Rollout

### Phase 1 — Foundation
1. Add `AppType` enum + field to `core:build-config`. Update `AppBuildConfigSmokeTest`.
2. Create `kioskApp` AGP application module (Application + Activity + Compose entry call + manifest + icons + theme + flavor matrix).
3. Create `kioskComposeApp` KMP library (Android target only, Compose root, `ProdKioskAppGraph`, SQLDelight aggregator, `KioskIdleTimer` skeleton).
4. Update `settings.gradle.kts` (include new modules + kiosk feature auto-discovery walk + filter consumer walk).
5. Update `composeApp/build.gradle.kts` (add `&& it.name != "kiosk"` filter to its two walks).
6. Add new Konsist rules.
7. Verify `:kioskApp:assembleUsIntDebug` builds end-to-end. Empty Compose root is fine — no features yet.

### Phase 2 — `features/kiosk/attract`
1. Scaffold the 6-module split (or use `/add-feature` if it supports nested paths).
2. Implement `AttractContent` model, `GetAttractContent` use case, `AttractRepositoryImpl` (static fallback ad list).
3. Implement `AttractPresenter`, `AttractUi.kt` (`HorizontalPager` + tap detector + branding overlay).
4. Wire `AttractScreen` as the kiosk navigation root in `MockDonaldsKioskApp`.
5. Tests: presenter, repo, use case, Fake, UI component robot triplet.

### Phase 3 — `features/kiosk/identify`
1. Scaffold the 6-module split.
2. Implement `IdentifyContent`, `GetIdentifyContent`, `IdentifyByPhoneNumber` (stubbed — returns deterministic identified result), `ContinueAsGuest`, `IdentifyByQrCode` (stubbed — returns Failed("not_implemented")).
3. Implement `IdentifyPresenter`, `IdentifyUi.kt` (keypad + skip CTA + QR placeholder).
4. Wire `Attract.TouchToOrder` → `goTo(IdentifyScreen)`.
5. Tests.

### Phase 4 — `features/kiosk/order`
1. Extend `features/order/api/domain.OrderModels` additively (new `MenuItem` model, `itemsByCategory` map, optional `iconUrl` on `MenuCategory`). Update `OrderRepositoryImpl` to populate. Verify consumer order screen still builds and renders.
2. Scaffold `features/kiosk/order` 6-module split.
3. Implement `KioskOrderPresenter` consuming `GetOrderContent`.
4. Implement `KioskOrderUi.kt` (NavigationRail + grid + cart bar).
5. Wire `Identify` results → `resetRoot(KioskOrderScreen)`.
6. Tests.

### Phase 5 — Idle reset wiring
1. Implement `KioskIdleTimer` with remote-config-driven timeouts.
2. Implement `IdleResetEffect` in `MockDonaldsKioskApp`.
3. Tests: timer unit test, navint coverage of expiry → resetRoot.

### Phase 6 — Polish & verification
1. Run `verify all` across all 30 kiosk variants.
2. Add kiosk macrobenchmark.
3. Add kiosk E2E journey test.
4. Document deployment / lock-task setup in `kioskApp/AGENTS.md`.

### Future (out of scope for v1)
- CameraX + ML Kit Barcode QR scanner (phase 3 follow-up).
- Real `/v1/kiosk/identify` backend wiring.
- `KioskItemDetailScreen`, `KioskCartReviewScreen`, `KioskOrderTypeScreen` (Eat-in / Take-out), `KioskPaymentScreen`, `KioskReceiptScreen`.
- Printer integration (receipt + order number).
- Per-store remote-config overrides (terminal-specific configuration).
- Multi-language attract loop (es-US, fr-CA, de-DE) via Phrase.
- iOS kiosk port (would add iOS targets to `kioskComposeApp` + new `kioskIosApp` Xcode project).
- Hardware variability: landscape SKU support via window-size-class branching.

---

## Constraints & Considerations

- **Backend gap**: `/v1/kiosk/identify` likely doesn't exist. Phase-1 stubs with deterministic responses; document the stub clearly in `features/kiosk/identify/AGENTS.md`. Memory: backend APIs sit behind Akamai in all envs — kiosk identify will need that header too, which `core:network:impl` already handles.
- **No deep links**: kiosk launches from the launcher only. Manifest has no `<intent-filter>` for `mockdonalds://` URIs.
- **No auth interceptor**: kiosk has no `ProtectedScreen` — identification is opportunistic. `KioskApp.kt` does not wire `AuthInterceptor`.
- **No bottom-nav / no `TabScreen`**: Konsist rule enforces. Visual NavigationRail in KioskOrder is screen-internal Material UI, not a Circuit-level tab.
- **Idle reset clears cart**: when `IdleResetEffect` fires `resetRoot(AttractScreen)`, the kiosk-owned cart store (`features/kiosk/order/impl/data`) observes the screen change and discards session state. Implementation detail: a `SessionScope` keyed off the kiosk session id, invalidated on idle.
- **Single-process Android sandbox**: kiosk and consumer have different `applicationId`s; they cannot share data directly. SQLDelight `AppDatabase` instances are per-app.
- **Hardcoded English strings v1**: every kiosk-specific string lives in `core:strings` keyed by `kiosk_*` prefix. English defaults; pulled via Phrase when localization wires up. The bilingual "Touch to Order / Tocar para Ordenar" overlay shown in Image 1 is intentionally deferred — v1 is English-only.
- **R8 / proguard**: kiosk applies the same proguard rules as `androidApp`, plus any kiosk-specific rules (none anticipated v1).

---

## Out of Scope

- CameraX + ML Kit QR scanner integration (interface exists; impl stubbed).
- Real `/v1/kiosk/identify` backend integration (stubbed responses v1).
- Cart, payment, receipt, item-detail screens (placeholder no-ops in `KioskOrderEvent` handlers).
- iOS kiosk port.
- Lock-task DPC integration (documented; deployment-infra concern).
- Multi-language attract loop and full localization.
- Per-store / per-terminal remote-config overrides.
- Printer integration.
- Landscape kiosk hardware support.
- Cross-app data sharing between kiosk and consumer.

---

## Decisions

| # | Question | Decision | Rationale |
|---|---|---|---|
| 1 | Reuse `composeApp` host or build a peer host? | **Peer `kioskComposeApp` library**. | composeApp's auto-discovery would mix kiosk + consumer feature classes at compile time; peer host gives clean static separation. User flagged this directly: "kiosk wouldn't use all feature modules, and androidapp wouldn't use kiosk feature modules, this is an issue for compile and dependencies". |
| 2 | Refactor `composeApp` to extract shared host primitives now? | **No — defer**. Build kiosk standalone with its own (smaller) navigation surface; revisit shared extraction if duplication becomes painful. | YAGNI. Kiosk doesn't need `AuthInterceptor`, `DeepLinkParser`, or even `InterceptingNavigator` initially. User: "do we need to just keep it separate and re-eval once it up and going?" |
| 3 | Flat `features/kiosk-*` or nested `features/kiosk/{name}` dirs? | **Nested `features/kiosk/{name}/...`**. | User preference; cleaner directory grouping; filter is a directory check (`name != "kiosk"`) instead of a string-prefix check. |
| 4 | Is the kiosk's category sidebar a Circuit `TabScreen` or screen-internal UI? | **Screen-internal `NavigationRail`**, not Circuit tabs. | Categories are presenter state (same shape as the consumer order screen), not separate navigation destinations. The kiosk host scaffold stays bare; rail lives in `features/kiosk/order/impl/presentation`. |
| 5 | Reuse `features/order` or duplicate the menu domain? | **Reuse `api/domain` + `impl/{data, domain}`; duplicate `impl/presentation`**. | Same menu data on both apps; different UI. Extending `OrderContent` additively is consumer-safe. |
| 6 | iOS support v1? | **No** — Android-only. `kioskComposeApp` declares only the Android target. | User: "I want to add another android app for a kiosk solution". iOS port is purely additive to `kioskComposeApp` later if needed. |
| 7 | Real `/v1/kiosk/identify` integration v1? | **Stubbed** — deterministic identified-by-phone result, `IdentifyByQrCode` returns `Failed("not_implemented")`. | Backend likely doesn't have the endpoint; ship the UX layer first; swap to real network in a follow-up without breaking presenter contracts. |
| 8 | Camera/QR scanner v1? | **Deferred (real CameraX)**; UI is a simulated tappable placeholder. *(See grilled row 23 for the permission-declaration update — superseded.)* | Reduces phase-1 scope; CameraX + ML Kit is an additive change later behind the existing flag. |
| 9 | Idle timer location? | **`kioskComposeApp` (commonMain)**, with timeouts driven from `core:remote-config`. | Non-feature, host-level concern; lives in the host module. Timeouts per-screen via remote-config so ops can tune without a release. |
| 10 | New build-config field for kiosk-vs-consumer? | **Yes — `appType: AppType` in `core:build-config:api`**, default `Consumer`, kiosk app sets `Kiosk`. | Enables runtime branching in shared code (analytics events, feature flags, log tagging) without dragging in module-graph branches. |
| 11 | Kiosk navigation root? | **`AttractScreen`** as the saveable backstack root. | First screen the user sees on launch; idle resets always restore to it. |
| 12 | What does the kiosk "back" button do? | **`navigator.resetRoot(AttractScreen)`** from KioskOrder. | Kiosk has no parent stack; back means "abandon and reset". |
| 13 | New AGP application module or reuse `androidApp` with a flavor switch? | **New `kioskApp` AGP application module**. | Different `applicationId`, manifest, theme, launcher activity, dependencies. AGP flavors can't change `implementation(project(...))` declarations. |
| 14 | Single shared `AppDatabase` across kiosk + consumer? | **No — separate `AppDatabase` per app, aggregated in each Compose host**. | Different Android sandboxes; can't share SQLite anyway. Each host owns its aggregation. |
| 15 | `MoreTabExtension`-style debug-menu integration for kiosk? | **No**. Kiosk has no `More` surface; debug-menu is consumer-only. | Out of scope for kiosk MVP. A separate kiosk-debug-overlay (long-press corner gesture) could be added later if needed for in-restaurant troubleshooting. |
| 16 *(grilled)* | Should kiosk mirror the consumer's full 30-variant matrix (5 markets × 3 envs × 2 build types + benchmark), or narrow it? | **Mirror — full 30 variants**. | The market/env variations represent the same shared software adapting to different deployments, not separate codebases. Each kiosk hardware unit ships one variant, but the build matrix mirrors how kiosks adapt across the global fleet. Kept symmetric with `BuildVariantResolver`'s existing regex and consumer CI. |
| 17 *(grilled)* | `applicationId` base — sibling, nested, or shared with consumer? | **`com.mockdonalds.kiosk`** (sibling to `com.mockdonalds.app`). | Reads as a separate product line on a device's app list (which it is); keeps consumer namespace untouched; matches the project's existing pattern of `com.mockdonalds.{surface}` for distinct AGP namespaces. |
| 18 *(grilled)* | `OrderContent` schema — extend additively, isolate kiosk in its own model, or defer? | **Extend `features/order/api/domain` additively**, framed as menu-domain evolution (not kiosk-specific contortion). | Real menu data is hierarchical per-market JSON; the current `OrderContent` shape is a placeholder. Both consumer and kiosk render by category, so the extension serves both surfaces. New shape: `MenuItem(id, categoryId, name, priceFormatted, calories, imageUrl, description, tags)` + `itemsByCategory: Map<String, List<MenuItem>>` on `OrderContent`; existing `featuredItems` retained for backward compat. Future backend integration will expand further (modifiers, variants, allergens) — same domain, additive growth. |
| 19 *(grilled)* | Country dial-code source of truth for the identify keypad? | **Add `phoneCountryDialCode` as a per-market field in `core:build-config`** (`/add-config-field`). | Matches every other per-market config field's pattern (`locale`, `currency`, `baseUrl`); compile-time per-market resolution; `IdentifyRepositoryImpl` just reads `appBuildConfig.phoneCountryDialCode`. Per-market values: `us`/`ca` → `+1`, `de` → `+49`, `au` → `+61`. |
| 20 *(grilled)* | Attract `Ad` model — keep `backgroundColorHex`? Bilingual overlay v1? | **Drop `backgroundColorHex`**; **English-only overlay** (`"Touch to Order"`) v1, defer bilingual. | `backgroundColorHex` was speculative — full-bleed photography from Image 1/2 covers the screen, no flat backdrops needed. Bilingual handling depends on Phrase / per-market string wiring that isn't in scope for kiosk MVP; defer until localization infra is addressed. |
| 21 *(grilled)* | KioskOrder "Back" button + "Cancel" cart-bar button v1 behavior? | **Both `BackPressed` and `CancelOrderPressed` → `resetRoot(AttractScreen)`** in v1. | No parent screen exists post-identify; "back" means "abandon the order." Both buttons collapse to the same session-reset action until item-detail / cart-review sub-screens land (then "back" becomes intra-order navigation, "cancel" stays session-reset). |
| 22 *(grilled)* | Idle-timeout defaults per screen? | **Attract: 0s (disabled), Identify: 60s, Order: 120s.** All overridable via `kiosk.${screen}.idle_timeout_seconds` remote-config. | Identify is fast (phone-entry scale), so 60s is generous; order is browse-heavy, so 120s lets guests decide; attract is already the idle state. Lands in the 60-180s industry-norm band for QSR kiosks. |
| 23 *(grilled)* | Camera permission + QR-scanner UI v1? | **Skip camera permission entirely in v1; simulate the QR-scanner half of the identify screen.** | No `<uses-permission CAMERA>` in `kioskApp/AndroidManifest.xml` until CameraX integration actually ships. The QR-scanner UI tile renders a faux viewfinder + tap dispatches a simulated `QrCodeDetected("simulated-payload-${timestamp}")` so the downstream identify flow is exercisable end-to-end without hardware. Lets us iterate on identify UX without waiting on camera infra. |
| 24 *(grilled)* | Static fallback ad list — count, content, sourcing assumption? | **3 placeholder ads** with synthetic copy matching `OrderRepositoryImpl`'s fake-data tone (Midnight Truffle / Saffron Fry / Signature Line); image URLs use the `https://example.test/` sentinel domain. | Production ad delivery in a real fleet likely comes from a marketing-content platform (Braze or similar) feeding remote-config or a dedicated ad endpoint; the `AttractRepository` interface is the swap seam. v1 just needs enough fallback content to look populated when remote-config is empty. Keeps the project's deliberate fake-data hygiene (no real-brand IP in synthetic content). |
| 25 *(grilled)* | `/add-feature` skill — nested-path support? | **Skill assumes flat `features/{name}/...`**; scaffold the 3 kiosk feature modules manually for this initiative. **Follow up post-implementation**: assess whether to extend `/add-feature` to support a `--parent` or path argument so future kiosk features can use the skill. | Inspected `.claude/skills/add-feature/SKILL.md` — module-creation step hardcodes `features/{name}/...`. Manual scaffolding for 3 features × 6 modules = 18 small build-script + AGENTS.md + Kotlin stamping; not painful, and gives full control over the auto-discovery walk for the kiosk subdir. Skill update is a tooling change worth doing if kiosk grows beyond ~3 features. |
| 26 *(implementation)* | Test infra modules — flat or nested under `testing/kiosk/`? | **Nested: `:testing:kiosk:navint-tests`, `:testing:kiosk:e2e-tests`, `:testing:kiosk:benchmarks`**. Konsist rules added as a `kiosk/` subpackage in `:testing:architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/kiosk/`. | Mirrors the `features/kiosk/{name}/` directory grouping — kiosk's test infra peers cleanly with the consumer's `testing/{navint-tests,e2e-tests,benchmarks}` set. |
| 27 *(implementation)* | `IdentifyRepository` one-shot suspend functions vs Flow-only? | **Repository stays Flow-only (`getIdentifyContent(): Flow<IdentifyContent>`)**; one-shot identify side-effects move into the use-case impls themselves, which call `core:auth.AuthManager` directly. | Discovered during phase-3 implementation when `DataLayerTest` Konsist rule rejected `suspend fun identifyByPhone(...)` on the repository interface (the rule mandates Flow-only repository methods). Refactor matches how `LoginRepository` handles "submit" actions — login goes through `AuthManager.login()` directly from the presenter, never through the repository. |
| 28 *(implementation)* | `appType: String` vs `appType: AppType` (enum) on `AppBuildConfig`? | **String** with documented allowed values `"Consumer"`/`"Kiosk"` + `isKiosk` extension property. | The KSP fake processor at `:build-tooling:ksp-fake-app-build-config` only handles primitives (String/Int/Boolean/etc.); an `AppType` enum would crash its type-empty-default generator. Mirrors the existing `buildType: String` + `isDebug` extension pattern. |

---

## Original Requirements

> **Source**: design discussion (inline conversation between user and assistant, this thread)
> **Converted on**: 2026-04-30
> **Spec type**: `new`

The user requested a separate Android app target for an in-restaurant kiosk solution that shares core modules with the consumer app, with three sequential screens:

1. **Ad/attract splash** showing rotating promotional content (per Image 1, Image 2 — full-bleed product photography with "Order Here" / "Touch to Order / Tocar para Ordenar" overlay).
2. **Identify screen** with QR-code scan or phone-number entry, plus an "opt-out / continue without account" path (per Image 2 — "Start Order" + "Earn Points / Scan now" pair).
3. **Order screen** with a vertical category `NavigationRail` and multi-column item grid + cart bar (per Image 3, Image 4 — "Burgers" with sidebar of Beverages/Happy Meals/Sandwiches & Meals/etc., 3-col item grid with prices and calories, "Around the World Menu" 3-col grid with $0.00 / Cart / Pay bottom strip).

The user explicitly steered the architecture through several discussions:

- **Reuse vs. new feature decisions**: confirmed the kiosk should reuse the menu domain layer of `features/order` and add three new kiosk-specific feature modules.
- **Host architecture**: rejected reusing `composeApp` directly because of compile-time feature-graph mixing; chose a peer `kioskComposeApp` library instead.
- **Refactor scope**: rejected refactoring `composeApp` to extract shared host primitives upfront; agreed to keep kiosk standalone with its own (smaller) host surface and re-evaluate after kiosk ships.
- **Directory grouping**: chose nested `features/kiosk/{name}/...` over flat `features/kiosk-{name}` paths.
- **Navigation rail**: noted the kiosk's category sidebar visible in Image 3 / Image 4 is a Material `NavigationRail` rendered inside the order screen's UI, not a Circuit-level tab navigation primitive.
- **Kiosk graph**: confirmed kiosk gets its own `ProdKioskAppGraph` (`@DependencyGraph(AppScope::class)`) implementing `core:metro.AppGraph`, paralleling the consumer's `ProdAppGraph`.
- **Excluded concerns**: kiosk doesn't need `AuthInterceptor`, `DeepLinkParser`, `InterceptingNavigator`, or bottom-nav.

Image references (in order received):
- **Image 1** — vertical kiosks, "BACON MEETS OUR CLASSICS / Order Here" with Touch to Order / Tocar para Ordenar overlay.
- **Image 2** — kiosk in restaurant, "Try a Bacon Cajun Ranch McCrispy" with Start Order + Earn Points scan-now CTA pair at bottom.
- **Image 3** — Burgers menu screen, vertical category sidebar (Beverages, Happy Meals, Sandwiches & Meals, Main Menu, All Day Breakfast, Fries & Sides, Sweets & Treats, Condiments), 3-col item grid (Big Mac/Hamburger/McDouble/Quarter Pounder/Cheeseburger/Bacon McDouble/Quarter Pounder Cheese Deluxe/Double Cheeseburger/Double Quarter With Cheese/Triple Cheeseburger), Back + Scan Offer top-right buttons, HOME M-arches tile top-left.
- **Image 4** — "Around the World Menu", vertical category list (Home / Value Picks / Promotions / Around the World Menu / Customized Salad / Extra Value Meal / A la Carte Meals / Signature Collections / Beverages / Desserts / Happy Meals / McCafe), 3-col item grid (BLT Angus Burger Meal/BLT Crispy Chicken Burger Meal/Triple Cheeseburger Meal/Salad Burger Meal/BLT Angus Burger/BLT Crispy Chicken Burger/Triple Cheeseburger/Salad Burger/Bite Sized Choco Churros/McPops 2pcs Combo/McPops 2pcs/Espresso Orange/Espresso Tonic/Espresso Lemonade/$0.85 Beer), Scan-now M-arches tile bottom-left, $0.00 cart total + Cancel + Pay bottom strip.
