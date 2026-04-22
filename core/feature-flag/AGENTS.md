# core:feature-flag

## Purpose

Runtime feature flag system with provider-agnostic remote SDK abstraction, split into `api`, `impl`, and `test` submodules. Features depend only on `api`; the concrete implementation and remote provider are provided at runtime via Metro DI. Production binding uses Harness Feature Flags (iOS via Swift bridge, Android via direct SDK).

## Architecture

```
core/feature-flag/api            -> FeatureFlagProvider interface, FeatureFlag type, rememberFlag extension
core/feature-flag/impl           -> RemoteFeatureFlagSource abstraction + Harness impls per platform
  └ commonMain                     RemoteFeatureFlagSource interface, FeatureFlagProviderImpl, FeatureFlagBuildConfig (BuildKonfig-generated)
  └ androidMain                    HarnessRemoteFeatureFlagSourceImpl → CfClient (io.harness:ff-android-client-sdk)
  └ iosMain                        HarnessRemoteFeatureFlagSourceImpl → HarnessIosBridge (interface)
  └ swift/                         Swift package "FeatureFlagBridge" — HarnessClient (fan-out wrapper around ff-ios-client-sdk); no ComposeApp import
  └ config/{int,mte,prod}.properties → HARNESS_CLIENT_ID per env → BuildKonfig
iosApp/iosApp/Harness/SwiftHarnessBridge.swift -> iosApp-target adapter: conforms to HarnessIosBridge (Kotlin protocol), wraps HarnessClient
core/feature-flag/test           -> FakeFeatureFlagProvider for consumer tests (presenter, domain, data)
```

## Public API

| Type | Module | Description |
|------|--------|-------------|
| `FeatureFlag` | api | Data class with `key: String` and `defaultValue: Boolean` |
| `FeatureFlagProvider` | api | Interface with `isEnabled(flag): Boolean` and `observe(flag): Flow<Boolean>` |
| `FeatureFlagProvider.rememberFlag(flag)` | api | `@Composable` extension returning `State<Boolean>` — the presenter-only entry point |
| `FeatureFlags` | api | Object for cross-cutting flag definitions (feature-specific flags go in their own `api/domain`) |
| `FeatureFlagDefinition` | api | Interface wrapping a `FeatureFlag` with registry metadata (`description`, `owner`, `lifecycle: FlagLifecycle`). Every production flag must ship a `@ContributesIntoSet(AppScope::class)` definition so the debug-menu feature-flag viewer can enumerate it. Consumers still read values via `rememberFlag` / `isEnabled` using `def.flag`. |
| `FeatureFlagDefinitionProviders` | impl | `@ContributesTo(AppScope::class)` interface declaring `@Multibinds(allowEmpty = true) fun featureFlagDefinitions(): Set<FeatureFlagDefinition>`. Lets Metro resolve the registry as an empty set when no flags have been contributed yet. |
| `FlagLifecycle` | api | Enum — `Experiment`, `KillSwitch`, `Ops`, `Permanent`. Drives filtering and retirement signals in the debug UI. |
| `FakeFeatureFlagProvider` | test | `MutableStateFlow`-backed fake with `setEnabled()` and `reset()` — drives both presenter (via `rememberFlag`) and domain/data tests |
| `HarnessIosBridge` | impl/iosMain | Vendor-agnostic-looking contract for iOS; implemented in Swift (`SwiftHarnessBridge`) |
| `FeatureFlagBuildConfig` | impl (generated) | BuildKonfig object exposing `HARNESS_CLIENT_ID` to Kotlin and Swift |

## Usage

**Presenters** inject `FeatureFlagProvider` and call `rememberFlag(flag)` per flag — one DI param, one line per flag, Compose-scoped state. Direct `.isEnabled(...)` / `.observe(...)` calls from presentation are Konsist-forbidden.

```kotlin
@CircuitInject(MyScreen::class, AppScope::class)
@Inject
@Composable
fun MyPresenter(
    featureFlags: FeatureFlagProvider,
): MyUiState {
    val newFeature   by featureFlags.rememberFlag(MyFlags.NEW_FEATURE)
    val experiment   by featureFlags.rememberFlag(MyFlags.EXPERIMENT)
    // ...
}
```

Why not a CenterPost interactor (like other core modules)? Flag reads are cheap, synchronous at the source, and routinely plural per screen. A `CenterPostSubjectInteractor` is shaped for "one param, one stream" and doesn't scale to N flags without N injections. The Composable extension gives one-line reads with per-flag recomposition isolation. See `.agents/standards/centerpost.md` for the documented carve-out.

**Domain/data layers** inject `FeatureFlagProvider` directly for synchronous checks:

```kotlin
@ContributesBinding(AppScope::class)
class MyRepositoryImpl(
    private val featureFlags: FeatureFlagProvider,
) : MyRepository {
    override fun getData(): Flow<Data> {
        val endpoint = if (featureFlags.isEnabled(MyFlags.NEW_API)) "/v2/data" else "/v1/data"
        // ...
    }
}
```

**Feature-specific flags** are defined in `features/{name}/api/domain/` and contributed to the registry via `FeatureFlagDefinition`:

```kotlin
object MyFlags {
    val NEW_FEATURE = FeatureFlag(key = "new_feature", defaultValue = false)
}

@ContributesIntoSet(AppScope::class)
class NewFeatureFlagDefinition : FeatureFlagDefinition {
    override val flag = MyFlags.NEW_FEATURE
    override val description = "Enables the rewritten feature flow"
    override val owner = "{team-or-feature}"
    override val lifecycle = FlagLifecycle.Experiment
}
```

The `FeatureFlag` stays the reading API (`rememberFlag(MyFlags.NEW_FEATURE)`); the `FeatureFlagDefinition` contribution adds enumeration metadata. Keys are namespaced (`order.checkout_v2`) so the debug UI can group by feature and the Harness dashboard stays readable.

## Harness integration

- **Client ID**: lives in `impl/config/{env}.properties` → BuildKonfig-generated `FeatureFlagBuildConfig.HARNESS_CLIENT_ID`, readable from both Kotlin and Swift. Swift reads via `FeatureFlagBuildConfig.shared.HARNESS_CLIENT_ID` (ObjC-exposed via BuildKonfig's `exposeObjectWithName`).
- **Android**: `HarnessRemoteFeatureFlagSourceImpl` (androidMain) injects `Application`, initializes `CfClient` directly, bridges listener callbacks to `Flow` via `callbackFlow`. The Harness Android AAR declares a `coreLibraryDesugaring` requirement in its metadata, so `androidApp/build.gradle.kts` enables `isCoreLibraryDesugaringEnabled = true` and adds the `desugar_jdk_libs` runtime dependency — removing this breaks `:androidApp:checkDebugAarMetadata`.
- **iOS**: `HarnessRemoteFeatureFlagSourceImpl` (iosMain) depends on `HarnessIosBridge` interface. The Swift side is split in two because local SPM packages cannot import the `ComposeApp` framework (it is produced by Gradle's `embedAndSignAppleFrameworkForXcode` and only linked into the `iosApp` Xcode target):
  - `HarnessClient` (SPM, `core/feature-flag/impl/swift/`) wraps `CfClient.sharedInstance`, exposes `boolVariation` + `registerListener` with per-subscription cancellation.
  - `SwiftHarnessBridge` (iosApp target, `iosApp/iosApp/Harness/`) conforms to the Kotlin `HarnessIosBridge` protocol and delegates to `HarnessClient`; it is provided to Kotlin via `ProdAppGraph.Factory.create(harnessIosBridge:)` from `AppDelegate`.
- **iOS listener fan-out**: Harness iOS SDK 1.3.4 has no per-listener unregister — only a global `clearEventsListener()`. `HarnessClient` registers **one** SDK-level `"*"` listener and maintains its own `[key: [UUID: callback]]` subscriber table. Cancelling a Flow removes that UUID from the table (no accumulation, no leaks). Kotlin applies `distinctUntilChanged()` so the per-key fan-out does not re-emit unchanged values.
- **Graph wiring**: `ProdAppGraph` is platform-specific (`composeApp/{androidMain,iosMain}/AppGraph.kt`). Android factory takes `Application`; iOS factory takes `HarnessIosBridge`. No `ProdAppGraph` in commonMain.

## Rules

- Core modules never import from features
- Features MUST depend on `core:feature-flag:api` only, never `core:feature-flag:impl`
- `impl` is wired exclusively through Metro `@ContributesBinding` in `AppScope`
- **Presenters** must read flags via `FeatureFlagProvider.rememberFlag(flag)` — direct `.isEnabled(...)` / `.observe(...)` calls in presentation are Konsist-forbidden
- **Domain/data layers** must use `FeatureFlagProvider.isEnabled(...)` or `.observe(...)` — `rememberFlag` is Composable and Konsist-forbidden outside presentation
- Test code should use fakes from `core:feature-flag:test`
- **Vendor carve-out**: impls of `RemoteFeatureFlagSource` are named `{Vendor}RemoteFeatureFlagSourceImpl` (e.g., `HarnessRemoteFeatureFlagSourceImpl`). The interface stays vendor-neutral; swapping vendors means adding a new `{Vendor}RemoteFeatureFlagSourceImpl` without touching consumers.
- **iOS bridge carve-out**: Harness iOS SDK is used from Swift (no CocoaPods, no expect/actual). Kotlin declares `HarnessIosBridge` interface; Swift implements in the colocated Swift package. Avoid growing the bridge surface — it should hold only what `RemoteFeatureFlagSource` needs.
