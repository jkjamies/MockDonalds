---
name: add-feature-flag
description: "Add a feature flag to gate behavior — flag definition, presenter observation, and domain/data checks. Use when adding runtime toggles for features or experiments. Production binding is Harness (iOS via Swift bridge, Android via direct SDK)."
---

# Add Feature Flag

> **Infrastructure status**: `core:feature-flag` provides `FeatureFlag` data class, `FeatureFlagProvider` interface (synchronous `isEnabled` + reactive `observe(flag): Flow<Boolean>`), and the Composable extension `FeatureFlagProvider.rememberFlag(flag): State<Boolean>` (presenter-only entry point — Konsist forbids direct `.isEnabled(...)` / `.observe(...)` calls in presentation). Production binding is Harness Feature Flags — `HarnessRemoteFeatureFlagSourceImpl` on Android (direct SDK via `io.harness:ff-android-client-sdk`) and on iOS (Kotlin interface `HarnessIosBridge` implemented by `SwiftHarnessBridge` in the colocated Swift package `core/feature-flag/impl/swift/`). Client ID per env comes from BuildKonfig (`FeatureFlagBuildConfig.HARNESS_CLIENT_ID`).

Add a feature flag to gate behavior in a feature.

**Parameters**: feature name, flag description (optional if spec provides them)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — feature name + flag description, e.g., `/add-feature-flag deals gate new deals carousel`.
2. **`@file` reference** — e.g., `/add-feature-flag @specs/deals-flags.md`. Extract flag keys, defaults, gating behavior, and affected UI from the spec. Template: `.agents/templates/new-spec.md` (Feature Flags section) or `.agents/templates/change-spec.md` (Feature Flag Changes section).
3. **Inline description** — free text describing flags and their purpose.

## Pre-flight: Grill the Spec

When the user provides a spec via `@file`, scan it for unresolved markers before scaffolding (see the grill-me skill for the full marker list): `<!-- TODO -->` placeholders, empty `- [ ]` AC items, empty required header fields, raw template placeholder prose, `...` table cells, or unconfirmed reverse-spec presumptions (`presumably` / `appears to`). If any are present, **stop and run `/grill-me @{spec}` first**, then resume this skill.

The conversion skills (`/ac-to-spec`, `/reverse-spec`) grill inline before producing output, so a marker-laden spec usually means the spec was hand-authored from a template or has gone stale. Skip the pre-flight only if the user explicitly says "skip the grill" — in that case, surface unresolved markers as `// TODO` comments in the generated code and call them out in the final summary.

## Reference

- Core feature-flag module: `core/feature-flag/AGENTS.md`
- `FeatureFlag` definition: `core/feature-flag/api/src/commonMain/.../FeatureFlag.kt`
- `FeatureFlagProvider` interface: `core/feature-flag/api/src/commonMain/.../FeatureFlagProvider.kt`
- `rememberFlag` Composable extension: `core/feature-flag/api/src/commonMain/.../RememberFlag.kt`
- Carve-out rationale (why no CenterPost interactor for flags): `.agents/standards/centerpost.md` → "Carve-out: core:feature-flag"

## Files to Create / Modify

### 1. Flag Definition — `api/domain/`

`features/{feature}/api/domain/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/api/domain/{Feature}Flags.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.api.domain

import com.mockdonalds.app.core.featureflag.FeatureFlag
import com.mockdonalds.app.core.featureflag.FeatureFlagDefinition
import com.mockdonalds.app.core.featureflag.FlagLifecycle
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

object {Feature}Flags {
    val {flagName} = FeatureFlag(
        key = "{feature}.{flag_name}",
        defaultValue = false,
    )
}

@ContributesIntoSet(AppScope::class)
class {FlagName}Definition : FeatureFlagDefinition {
    override val flag = {Feature}Flags.{flagName}
    override val description = "{one-line description for the debug UI}"
    override val owner = "{feature}"
    override val lifecycle = FlagLifecycle.Experiment  // or KillSwitch / Ops / Permanent
}
```

**Naming convention (namespaced)**: `{feature}.{snake_case_description}`
- `deals.carousel_enabled` — gates a UI section
- `deals.v2_api` — gates an API version
- `deals.experiment_new_layout` — A/B experiment

The dot-namespace groups flags by feature in the debug menu and Harness dashboard.

**Registry contribution is mandatory.** Every production flag must ship a `FeatureFlagDefinition` via `@ContributesIntoSet(AppScope::class)` so the debug-menu feature-flag viewer can enumerate it. The `FeatureFlag` itself is still the read API (`rememberFlag(def.flag)`); the definition just adds metadata.

### 2. Presenter Observation — `impl/presentation/`

Presenters inject `FeatureFlagProvider` and read each flag via the Composable extension `rememberFlag(flag)`. Direct `.isEnabled(...)` / `.observe(...)` calls from presentation are Konsist-forbidden — `rememberFlag` is the only presenter-facing read API.

```kotlin
@CircuitInject({Feature}Screen::class, AppScope::class)
@Inject
@Composable
fun {Feature}Presenter(
    featureFlags: FeatureFlagProvider,  // ← inject once, read N flags
    dispatchers: CenterPostDispatchers,
    // ... other dependencies
): {Feature}UiState {
    val centerPost = rememberCenterPost(dispatchers)

    // Reactive — UI updates when flag changes remotely. One line per flag.
    val isCarouselEnabled by featureFlags.rememberFlag({Feature}Flags.{flagName})
    // val otherFlag by featureFlags.rememberFlag({Feature}Flags.{otherFlag})

    return {Feature}UiState(
        showCarousel = isCarouselEnabled,
        // ...
    )
}
```

Scales flat: 1 flag or 10, the presenter signature doesn't change and each flag is one line. `rememberFlag` seeds the Compose state with `flag.defaultValue`, so the returned `State<Boolean>` is never null — no `?:` needed at the read site.

### 3. Domain/Data Checks (if needed) — `impl/domain/` or `impl/data/`

Domain and data layers inject `FeatureFlagProvider` directly and call `.isEnabled(...)` or `.observe(...)`. `rememberFlag` is Composable-only and Konsist-forbidden outside presentation.

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

Presenter tests inject `FakeFeatureFlagProvider` — the same fake serves both `rememberFlag` (presenter) and direct `.isEnabled(...)` / `.observe(...)` (domain/data) reads.

```kotlin
class {Feature}PresenterTest : BehaviorSpec({
    Given("carousel flag is enabled") {
        val featureFlags = FakeFeatureFlagProvider()
        featureFlags.setEnabled({Feature}Flags.carouselEnabled, true)
        // ... construct presenter with featureFlags, assert carousel shown
    }

    Given("carousel flag is disabled") {
        val featureFlags = FakeFeatureFlagProvider()
        featureFlags.setEnabled({Feature}Flags.carouselEnabled, false)
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
- **Every production flag must ship a `FeatureFlagDefinition` contribution** — `@ContributesIntoSet(AppScope::class)` — so the debug-menu enumerates it
- **Use namespaced keys** (`feature.flag_name`) so the registry can group by feature
- **Pick a `FlagLifecycle`**: `Experiment` for A/B tests, `KillSwitch` for operational toggles, `Ops` for infra knobs, `Permanent` for config that outlives a release
- **Presenters read flags via `featureFlags.rememberFlag(flag)`** — reactive Composable extension, one DI param for N flags
- **Domain/data use `FeatureFlagProvider.isEnabled(...)` / `.observe(...)`** — synchronous or Flow, direct injection
- **Never** call `.isEnabled(...)` / `.observe(...)` from presentation — Konsist-enforced
- **Never** reference `rememberFlag` from domain/data — Konsist-enforced (Composable-only)
- **Always test both flag states** — on and off
- **Default to `false`** — features are off until explicitly enabled (except kill switches)
- **Clean up flags** — when a flag is permanent, remove the flag and hardcode the behavior

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify` skill to validate all changes.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
