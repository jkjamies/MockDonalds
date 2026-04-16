---
name: add-feature-flag
description: "Add a feature flag to gate behavior — flag definition, presenter observation, and domain/data checks. Use when adding runtime toggles for features or experiments. NOTE: core:feature-flag exists with FeatureFlag, ObserveFeatureFlag, and FeatureFlagProvider — production remote config SDK integration is not yet finalized."
---

# Add Feature Flag

> **Infrastructure status**: `core:feature-flag` provides `FeatureFlag` data class, `ObserveFeatureFlag` CenterPost interactor (reactive, for presenters), `FeatureFlagProvider` interface (synchronous, for domain/data), and a default in-memory implementation. Production remote config SDK (e.g., LaunchDarkly, Harness) is planned but not yet wired — the provider abstraction is SDK-agnostic so the swap will be seamless.

Add a feature flag to gate behavior in a feature.

**Parameters**: feature name, flag description (optional if spec provides them)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — feature name + flag description, e.g., `/add-feature-flag deals gate new deals carousel`.
2. **`@file` reference** — e.g., `/add-feature-flag @specs/deals-flags.md`. Extract flag keys, defaults, gating behavior, and affected UI from the spec. Template: `.agents/templates/new-spec.md` (Feature Flags section) or `.agents/templates/change-spec.md` (Feature Flag Changes section).
3. **Inline description** — free text describing flags and their purpose.

## Reference

- Core feature-flag module: `core/feature-flag/AGENTS.md`
- `FeatureFlag` definition: `core/feature-flag/api/src/commonMain/.../FeatureFlag.kt`
- `ObserveFeatureFlag` interactor: `core/feature-flag/api/src/commonMain/.../ObserveFeatureFlag.kt`
- `FeatureFlagProvider` interface: `core/feature-flag/api/src/commonMain/.../FeatureFlagProvider.kt`

## Files to Create / Modify

### 1. Flag Definition — `api/domain/`

`features/{feature}/api/domain/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/api/domain/{Feature}Flags.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.api.domain

import com.mockdonalds.app.core.featureflag.FeatureFlag

object {Feature}Flags {
    val {flagName} = FeatureFlag(
        key = "{feature}_{flag_name}",
        defaultValue = false,
    )
}
```

**Naming convention**: `{feature}_{snake_case_description}`
- `deals_carousel_enabled` — gates a UI section
- `deals_v2_api` — gates an API version
- `deals_experiment_new_layout` — A/B experiment

### 2. Presenter Observation — `impl/presentation/`

Presenters use `ObserveFeatureFlag` CenterPost interactor (**never** `FeatureFlagProvider` directly — Konsist-enforced):

```kotlin
@CircuitInject({Feature}Screen::class, AppScope::class)
@Inject
@Composable
fun {Feature}Presenter(
    observeFeatureFlag: ObserveFeatureFlag,  // ← inject
    dispatchers: CenterPostDispatchers,
    // ... other dependencies
): {Feature}UiState {
    val centerPost = rememberCenterPost(dispatchers)

    // Reactive — UI updates when flag changes remotely
    val isCarouselEnabled by observeFeatureFlag.collectAsState(
        params = {Feature}Flags.{flagName},
    )

    return {Feature}UiState(
        showCarousel = isCarouselEnabled ?: {Feature}Flags.{flagName}.defaultValue,
        // ...
    )
}
```

### 3. Domain/Data Checks (if needed) — `impl/domain/` or `impl/data/`

Domain and data layers inject `FeatureFlagProvider` directly (**never** `ObserveFeatureFlag` — Konsist-enforced):

```kotlin
class {Name}Impl(
    private val featureFlagProvider: FeatureFlagProvider,  // ← inject
) : {Name}() {
    override fun createObservable(params: Unit): Flow<{Result}> {
        return if (featureFlagProvider.isEnabled({Feature}Flags.{flagName})) {
            repository.getNewData()
        } else {
            repository.getLegacyData()
        }
    }
}
```

### 4. UiState Changes

Add flag-derived fields to UiState:

```kotlin
data class {Feature}UiState(
    val showCarousel: Boolean = false,  // ← driven by feature flag
    // ... other fields
    val eventSink: ({Feature}Event) -> Unit,
) : CircuitUiState
```

### 5. UI Gating

In the Compose UI, conditionally render based on flag state:

```kotlin
if (state.showCarousel) {
    CarouselSection(items = state.carouselItems)
}
```

## Flag Types

| Type | Default | Use Case |
|------|---------|----------|
| Kill switch | `true` | Disable broken feature remotely — default ON, turn OFF if needed |
| Gradual rollout | `false` | Enable for % of users — default OFF, ramp up |
| Experiment | `false` | A/B test — default OFF, enable for test group |
| Temporary gate | `false` | WIP feature — default OFF until ready |

## Testing with Flags

### Unit Tests

```kotlin
class {Feature}PresenterTest : BehaviorSpec({
    Given("carousel flag is enabled") {
        val observeFeatureFlag = FakeObserveFeatureFlag()
        observeFeatureFlag.emit({Feature}Flags.carouselEnabled, true)
        // ... assert carousel shown
    }

    Given("carousel flag is disabled") {
        val observeFeatureFlag = FakeObserveFeatureFlag()
        observeFeatureFlag.emit({Feature}Flags.carouselEnabled, false)
        // ... assert carousel hidden
    }
})
```

### UI Tests

```kotlin
Given("carousel is enabled") {
    stateRobot.setShowCarousel(true)
    // ... assert UI shows carousel section
}
```

## Build File Dependencies

`api/domain/build.gradle.kts` needs:
```kotlin
commonMain.dependencies {
    api(project(":core:feature-flag:api"))
}
```

## Key Rules

- **Flags defined in `api/domain/`** — they're part of the feature's public contract
- **Presenters use `ObserveFeatureFlag`** — reactive, CenterPost interactor
- **Domain/data use `FeatureFlagProvider`** — synchronous, direct injection
- **Always test both flag states** — on and off
- **Default to `false`** — features are off until explicitly enabled (except kill switches)
- **Clean up flags** — when a flag is permanent, remove the flag and hardcode the behavior

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
