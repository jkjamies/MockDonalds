# core:remote-config

## Purpose

Runtime remote-configuration system with provider-agnostic remote SDK abstraction, split into `api`, `impl`, and `test` submodules. Features depend only on `api`; the concrete implementation and remote provider are provided at runtime via Metro DI. Production binding uses Harness Feature Flags (iOS via Swift bridge, Android via direct SDK).

The module exposes two complementary value types over a single source of truth:

- `FeatureFlag` — Boolean kill-switches and gating toggles
- `RemoteConfig<T>` — typed configuration values (`StringConfig`, `LongConfig`, `DoubleConfig`, `JsonConfig<T>`)

Both flow through the same `RemoteConfigProvider` interface and the same Harness backend; the type distinguishes intent at the call site.

## Architecture

```
core/remote-config/api            -> RemoteConfigProvider interface, FeatureFlag, RemoteConfig<T> sealed types,
                                     definition interfaces. Compose-free.
core/presentation/                -> rememberFlag / rememberConfig Composable extensions over RemoteConfigProvider
                                     (lives in :core:presentation, see core/presentation/AGENTS.md). Auto-wired into
                                     every feature impl/presentation module via sampleplatter.kmp.presentation plugin.
core/remote-config/impl           -> RemoteConfigSource abstraction + Harness impls per platform
  └ commonMain                       RemoteConfigSource interface, RemoteConfigProviderImpl, RemoteConfigBuildConfig (BuildKonfig-generated)
  └ androidMain                      HarnessRemoteConfigSourceImpl → CfClient (io.harness:ff-android-client-sdk)
  └ iosMain                          HarnessRemoteConfigSourceImpl → HarnessIosBridge (interface)
  └ swift/                           Swift package "RemoteConfigBridge" — HarnessClient (fan-out wrapper around ff-ios-client-sdk); no ComposeApp import
  └ config/{int,mte,prod}.properties → HARNESS_CLIENT_ID per env → BuildKonfig
iosApp/iosApp/Harness/SwiftHarnessBridge.swift -> iosApp-target adapter: conforms to HarnessIosBridge (Kotlin protocol), wraps HarnessClient
core/remote-config/test           -> FakeRemoteConfigProvider for consumer tests (presenter, domain, data)
```

## Public API

| Type | Module | Description |
|------|--------|-------------|
| `FeatureFlag` | api | Data class with `key: String` and `defaultValue: Boolean` — the boolean form |
| `RemoteConfig<T>` | api | Sealed hierarchy: `StringConfig`, `LongConfig`, `DoubleConfig`, `JsonConfig<T>` (carries a `KSerializer<T>`). Each subtype pins `T` so impls dispatch via exhaustive `when` |
| `jsonConfig<reified T>(key, default)` | api | Reified helper that builds `RemoteConfig.JsonConfig<T>` with the implicit `serializer<T>()`; payload type must be `@Serializable` |
| `RemoteConfigProvider` | api | Interface with `isEnabled(flag): Boolean`, `observe(flag): Flow<Boolean>`, `getConfig(config): T`, `observeConfig(config): Flow<T>` |
| `RemoteConfigProvider.rememberFlag(flag)` | `:core:presentation` | `@Composable` extension returning `State<Boolean>` — the presenter-only entry point for flags. Auto-wired via `sampleplatter.kmp.presentation` plugin. |
| `RemoteConfigProvider.rememberConfig(config)` | `:core:presentation` | `@Composable` extension returning `State<T>` — the presenter-only entry point for typed configs. Auto-wired via `sampleplatter.kmp.presentation` plugin. |
| `FeatureFlags`, `RemoteConfigs` | api | Empty registry objects for cross-cutting definitions (feature-specific keys live in their own `api/domain`) |
| `FeatureFlagDefinition` | api | Interface wrapping a `FeatureFlag` with registry metadata (`description`, `owner`, `lifecycle: FlagLifecycle`). Every production flag must ship a `@ContributesIntoSet(AppScope::class)` definition so the debug-menu viewer can enumerate it |
| `RemoteConfigDefinition<T>` | api | Parallel of `FeatureFlagDefinition` for typed configs — same metadata fields, carries a `RemoteConfig<T>` instead of a `FeatureFlag` |
| `FeatureFlagDefinitionProviders`, `RemoteConfigDefinitionProviders` | impl **and** test (parallel) | `@ContributesTo(AppScope::class)` interfaces declaring `@Multibinds(allowEmpty = true)` slots for `Set<FeatureFlagDefinition>` / `Set<RemoteConfigDefinition<*>>`. Declared **twice** — once in `impl` (for prod graphs) and once in `test` (same scope, same slot signature, different package) so test graphs that depend only on `api` + `test` still see the slots without pulling `impl`. Metro consolidates multibind slots by type + scope, so both declarations resolve to the same slot at compile time |
| `FlagLifecycle` | api | Enum — `Experiment`, `KillSwitch`, `Ops`, `Permanent`. Drives filtering and retirement signals in the debug UI for both flags and typed configs |
| `FakeRemoteConfigProvider` | test | `MutableStateFlow`-backed fake with `setEnabled(flag, bool)` and `setConfig<T>(config, value)` plus `reset()` — drives both presenter (via `rememberFlag` / `rememberConfig`) and domain/data tests |
| `HarnessIosBridge` | impl/iosMain | Vendor-agnostic-looking contract for iOS; implemented in Swift (`SwiftHarnessBridge`). Surface: `boolVariation`, `stringVariation`, `longVariation`, `doubleVariation`, `jsonVariation` (all return their respective primitives; JSON crosses as a `String`), plus `registerListener(key, () -> Unit)` (untyped change notification — Kotlin re-fetches via the typed methods) |
| `RemoteConfigBuildConfig` | impl (generated) | BuildKonfig object exposing `HARNESS_CLIENT_ID` to Kotlin and Swift |

## Usage

**Presenters** inject `RemoteConfigProvider` and call `rememberFlag(flag)` / `rememberConfig(config)` per key — one DI param, one line per key, Compose-scoped state. Direct `.isEnabled(...)` / `.observe(...)` / `.getConfig(...)` / `.observeConfig(...)` calls from presentation are Konsist-forbidden.

```kotlin
@CircuitInject(MyScreen::class, AppScope::class)
@Inject
@Composable
fun MyPresenter(
    remoteConfig: RemoteConfigProvider,
): MyUiState {
    val newFeature  by remoteConfig.rememberFlag(MyFlags.NEW_FEATURE)
    val maxRetries  by remoteConfig.rememberConfig(MyConfigs.MAX_RETRIES)
    // ...
}
```

Why not a Strata interactor (like other core modules)? Reads are cheap, synchronous at the source, and routinely plural per screen. A `StrataSubjectInteractor` is shaped for "one param, one stream" and doesn't scale to N keys without N injections. The Composable extension gives one-line reads with per-key recomposition isolation. See `.agents/standards/strata.md` for the documented carve-out.

**Domain/data layers** inject `RemoteConfigProvider` directly for synchronous checks:

```kotlin
@ContributesBinding(AppScope::class)
class MyRepositoryImpl(
    private val remoteConfig: RemoteConfigProvider,
) : MyRepository {
    override fun getData(): Flow<Data> {
        val endpoint = if (remoteConfig.isEnabled(MyFlags.NEW_API)) "/v2/data" else "/v1/data"
        val retries  = remoteConfig.getConfig(MyConfigs.MAX_RETRIES)
        // ...
    }
}
```

**Feature-specific keys** are defined in `features/{name}/api/domain/` and contributed to the registry via `FeatureFlagDefinition` / `RemoteConfigDefinition`:

```kotlin
object MyFlags {
    val NEW_FEATURE = FeatureFlag(key = "my.new_feature", defaultValue = false)
}

object MyConfigs {
    val MAX_RETRIES = RemoteConfig.LongConfig(key = "my.max_retries", default = 3L)
    val RETRY_POLICY = jsonConfig(key = "my.retry_policy", default = RetryPolicy(3, 100L))
}

@Serializable
data class RetryPolicy(val maxAttempts: Int, val backoffMs: Long)

@ContributesIntoSet(AppScope::class)
class NewFeatureFlagDefinition : FeatureFlagDefinition {
    override val flag = MyFlags.NEW_FEATURE
    override val description = "Enables the rewritten my-feature flow"
    override val owner = "{team-or-feature}"
    override val lifecycle = FlagLifecycle.Experiment
}

@ContributesIntoSet(AppScope::class)
class MaxRetriesConfigDefinition : RemoteConfigDefinition<Long> {
    override val config = MyConfigs.MAX_RETRIES
    override val description = "HTTP retry attempts before falling back"
    override val owner = "{team-or-feature}"
    override val lifecycle = FlagLifecycle.Ops
}
```

Keys are namespaced (`order.checkout_v2`, `my.max_retries`) so the debug UI can group by feature and the Harness dashboard stays readable. The `FeatureFlag` / `RemoteConfig<T>` instance stays the read API; the definition contribution adds enumeration metadata.

## Harness integration

- **Client ID**: lives in `impl/config/{env}.properties` → BuildKonfig-generated `RemoteConfigBuildConfig.HARNESS_CLIENT_ID`, readable from both Kotlin and Swift. Swift reads via `RemoteConfigBuildConfig.shared.HARNESS_CLIENT_ID` (ObjC-exposed via BuildKonfig's `exposeObjectWithName`).
- **Android**: `HarnessRemoteConfigSourceImpl` (androidMain) injects `Application`, initializes `CfClient` directly, bridges `EvaluationListener` callbacks to `Flow` via `callbackFlow`. Variation calls dispatch on the sealed `RemoteConfig<T>` subtype — see "Dashboard storage" below for the per-type Harness flag-type mapping. The Harness Android AAR declares a `coreLibraryDesugaring` requirement in its metadata, so `androidApp/build.gradle.kts` enables `isCoreLibraryDesugaringEnabled = true` and adds the `desugar_jdk_libs` runtime dependency — removing this breaks `:androidApp:checkDebugAarMetadata`.
- **iOS**: `HarnessRemoteConfigSourceImpl` (iosMain) depends on `HarnessIosBridge` interface. The Swift side is split in two because local SPM packages cannot import the `ComposeApp` framework (it is produced by Gradle's `embedAndSignAppleFrameworkForXcode` and only linked into the `iosApp` Xcode target):
  - `HarnessClient` (SPM, `core/remote-config/impl/swift/`) wraps `CfClient.sharedInstance`, exposes `boolVariation` / `stringVariation` / `numberVariation` / `jsonVariation` + `registerListener` with per-subscription cancellation.
  - `SwiftHarnessBridge` (iosApp target, `iosApp/iosApp/Harness/`) conforms to the Kotlin `HarnessIosBridge` protocol and delegates to `HarnessClient`; it is provided to Kotlin via `ProdAppGraph.Factory.create(harnessIosBridge:)` from `AppDelegate`.
- **iOS init lifecycle**: `HarnessClient.ensureInitialized()` runs a 4-state machine (`.notStarted` / `.inProgress` / `.initialized` / `.failed`) and only flips state from inside the SDK's initialize completion handler. Variation reads never block on init — until the SDK reports back, Harness returns the local default. When init completes (success or failure), `fanOutChange()` fires so any observers that subscribed pre-init re-fetch their current value via the typed variation methods.
- **iOS listener fan-out**: Harness iOS SDK 1.3.4 has no per-listener unregister — only a global `clearEventsListener()`. `HarnessClient` registers **one** SDK-level `"*"` listener and maintains its own `[key: [UUID: () -> Void]]` subscriber table. The bridge's `registerListener(key, () -> Unit)` is **untyped** — it fires void on any change, and Kotlin re-fetches the current value via the typed `boolVariation`/`stringVariation`/etc. methods. Cancelling a Flow removes that UUID from the table (no accumulation, no leaks). Kotlin applies `distinctUntilChanged()` so the fan-out does not re-emit unchanged values.
- **Dashboard storage**: each `RemoteConfig<T>` subtype maps to a specific Harness flag type. Configure flags in the Harness dashboard accordingly:

  | Kotlin type | Harness flag type | Reason |
  |---|---|---|
  | `FeatureFlag` | Boolean | native bool variation |
  | `RemoteConfig.StringConfig` | String | native string variation |
  | `RemoteConfig.DoubleConfig` | **String** (numeric content) | Harness `numberVariation` is typed inconsistently across SDKs (Android 2.2.7 → `Double` lossy above 2^53; iOS 1.3.x → `Int`, truncates fractional values). Routing through `stringVariation` keeps both platforms exact and symmetric. |
  | `RemoteConfig.LongConfig` | **String** (numeric content) | Same reason as `DoubleConfig` — `numberVariation` cannot represent every `Long` exactly on either platform. |
  | `RemoteConfig.JsonConfig<T>` | **String** (JSON-encoded content) | keeps platform-specific dict/JSONObject types out of the bridge surface; Kotlin handles encode/decode via `KSerializer<T>` |

  `DoubleConfig`, `LongConfig`, and `JsonConfig` are all routed through `stringVariation` under the hood on both platforms. `BooleanConfig` (i.e. `FeatureFlag`) and `StringConfig` use their natively-matching Harness types.
- **Graph wiring**: `ProdAppGraph` is platform-specific (`composeApp/{androidMain,iosMain}/AppGraph.kt`). Android factory takes `Application`; iOS factory takes `HarnessIosBridge`. No `ProdAppGraph` in commonMain.

## Rules

- Core modules never import from features
- Features MUST depend on `core:remote-config:api` only, never `core:remote-config:impl`
- `impl` is wired exclusively through Metro `@ContributesBinding` in `AppScope`
- **Presenters** must read via `RemoteConfigProvider.rememberFlag(flag)` / `rememberConfig(config)` — direct `.isEnabled(...)` / `.observe(...)` / `.getConfig(...)` / `.observeConfig(...)` calls in presentation are Konsist-forbidden
- **Domain/data layers** must use `RemoteConfigProvider.isEnabled(...)` / `.observe(...)` / `.getConfig(...)` / `.observeConfig(...)` — `rememberFlag` / `rememberConfig` are Composable and Konsist-forbidden outside presentation
- Test code should use fakes from `core:remote-config:test`
- **JSON payload types** must be `@Serializable` data classes; use the `jsonConfig<reified T>(key, default)` helper to build the `RemoteConfig.JsonConfig<T>` instance with the correct serializer
- **Vendor carve-out**: impls of `RemoteConfigSource` are named `{Vendor}RemoteConfigSourceImpl` (e.g., `HarnessRemoteConfigSourceImpl`). The interface stays vendor-neutral; swapping vendors means adding a new `{Vendor}RemoteConfigSourceImpl` without touching consumers.
- **iOS bridge carve-out**: Harness iOS SDK is used from Swift (no CocoaPods, no expect/actual). Kotlin declares `HarnessIosBridge` interface; Swift implements in the colocated Swift package. Avoid growing the bridge surface — it should hold only what `RemoteConfigSource` needs.
