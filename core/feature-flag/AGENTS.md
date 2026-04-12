# core:feature-flag

## Purpose

Runtime feature flag system with provider-agnostic remote SDK abstraction, split into `api`, `impl`, and `test` submodules. Features depend only on `api`; the concrete implementation and remote provider are provided at runtime via Metro DI.

## Architecture

```
core/feature-flag/api   -> FeatureFlagProvider interface, ObserveFeatureFlag interactor, FeatureFlag type (feature-visible)
core/feature-flag/impl  -> Resolution chain, remote source abstraction (DI-only, never imported directly)
core/feature-flag/test  -> FakeFeatureFlagProvider, FakeObserveFeatureFlag for consumer tests
```

## Public API

| Type | Module | Description |
|------|--------|-------------|
| `FeatureFlag` | api | Data class with `key: String` and `defaultValue: Boolean` |
| `FeatureFlagProvider` | api | Interface with `isEnabled(flag): Boolean` and `observe(flag): Flow<Boolean>` |
| `ObserveFeatureFlag` | api | Abstract `CenterPostSubjectInteractor<FeatureFlag, Boolean>` for presenter consumption |
| `FeatureFlags` | api | Object for cross-cutting flag definitions (feature-specific flags go in their own `api/domain`) |
| `FakeFeatureFlagProvider` | test | `MutableStateFlow`-backed fake with `setEnabled()` and `reset()` |
| `FakeObserveFeatureFlag` | test | `MutableStateFlow`-backed fake with `setEnabled()` and `reset()` |

## Usage

**Presenters** use the CenterPost interactor for reactive flag observation:

```kotlin
@CircuitInject(MyScreen::class, AppScope::class)
@Composable
fun MyPresenter(
    observeFeatureFlag: ObserveFeatureFlag,
): MyUiState {
    observeFeatureFlag(MyFlags.NEW_FEATURE)
    val newFeatureEnabled by observeFeatureFlag.flow.collectAsState(initial = false)
    // ...
}
```

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

**Feature-specific flags** are defined in `features/{name}/api/domain/`:

```kotlin
object MyFlags {
    val NEW_FEATURE = FeatureFlag(key = "new_feature", defaultValue = false)
}
```

## Rules

- Core modules never import from features
- Features MUST depend on `core:feature-flag:api` only, never `core:feature-flag:impl`
- `impl` is wired exclusively through Metro `@ContributesBinding` in `AppScope`
- **Presenters** must use `ObserveFeatureFlag` interactor, never `FeatureFlagProvider` directly (Konsist-enforced)
- **Domain/data layers** must use `FeatureFlagProvider`, never `ObserveFeatureFlag` (Konsist-enforced)
- Test code should use fakes from `core:feature-flag:test`
- Remote SDK is swappable: replace `DefaultRemoteFeatureFlagSource` binding in impl without touching api or consumers
