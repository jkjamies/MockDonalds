# Remove — Spec Template

> Copy this template, fill in the sections relevant to your removal, and delete sections
> that don't apply. Feed it to the `remove` skill via `@file` or paste inline.
>
> Usage: `/remove @specs/remove-legacy-rewards.md`

---

## Overview

<!-- One-paragraph summary: what's being removed and why. -->

**Target**: <!-- what's going away — feature, screen, use case, repository, core module, endpoint, etc. -->
**Target type**: <!-- feature | screen | use-case | repository | core-module | endpoint | config-field | other -->
**Feature / module**: <!-- which feature or module contains the target -->
**Reason**: <!-- deprecated | replaced by X | dead code | experiment ended | compliance -->

---

## What's Being Removed

<!-- Enumerate everything that should be deleted. Be exhaustive —
     partial removal is the #1 source of dead code. -->

### Files to Delete

<!-- List explicitly. The skill will verify each exists before deleting. -->

| File | Module | Purpose |
|------|--------|---------|
| `features/{name}/api/domain/{Thing}.kt` | api/domain | Model being removed |
| `features/{name}/impl/domain/{Thing}Impl.kt` | impl/domain | Implementation |
| `features/{name}/test/Fake{Thing}.kt` | test | Fake |
| ... | ... | ... |

### Directories to Delete (if entire modules)

- [ ] `features/{name}/` — entire feature
- [ ] `features/{name}/api/domain/` — submodule only
- [ ] ...

### Build File Changes

| File | Change |
|------|--------|
| `features/{name}/impl/domain/build.gradle.kts` | Remove dependency on deleted api module |
| `features/{other}/impl/presentation/build.gradle.kts` | Remove dependency on deleted feature api |
| ... | ... |

---

## Dependency Analysis

<!-- What depends on the thing being removed? This is the critical section —
     every reference must be updated or removed, or the build breaks. -->

### Direct Dependents

<!-- Modules/files that directly import or reference the target. -->

| Dependent | File | Reference | Action |
|-----------|------|-----------|--------|
| `features:{other}:impl:presentation` | `{Other}Presenter.kt` | Navigates to removed screen | Remove navigation / replace with alternative |
| `composeApp` | `AppNavGraph.kt` | Registers removed screen | Remove registration |
| `iosApp` | `{Feature}View.swift` | SwiftUI bridge for removed screen | Remove view + navigation |
| ... | ... | ... | ... |

### Transitive Dependents

<!-- Modules that don't directly reference the target but depend on something that does. -->

| Module | Impact | Action |
|--------|--------|--------|
| `testing:e2e-tests` | Journey references removed screen | Update or remove journey |
| `testing:navint-tests` | Nav test covers removed flow | Remove test |

### Navigation References

<!-- Screens that navigate TO the removed target. -->

| Source Screen | Navigation Call | Action |
|---------------|-----------------|--------|
| `HomeScreen` | `navigator.goTo({Removed}Screen)` | Remove or redirect |
| Deep link `mockdonalds:///{path}` | Routes to removed screen | Remove deep link |

---

## What Must Survive

<!-- Explicitly state what should NOT be removed — prevents overzealous cleanup. -->

| Keep | Reason |
|------|--------|
| `{Feature}Content` model | Still used by `features:{other}` |
| `{name}_base_url` config | Shared with another service |
| Analytics event history | Historical data in analytics platform is unaffected |
| ... | ... |

---

## Replacement (if applicable)

<!-- If the removed thing is being replaced, describe what takes its place. -->

**Replaced by**: <!-- new feature/screen/module, or "nothing — removing entirely" -->

**Migration path for dependents**:
| Dependent | Current Usage | New Usage |
|-----------|--------------|-----------|
| `{Other}Presenter` | Navigates to `{Removed}Screen` | Navigates to `{New}Screen` |
| `FakeGet{Removed}Content` | Used in `{Other}PresenterTest` | Replace with `FakeGet{New}Content` |

---

## Analytics Impact

<!-- What analytics events go away? Does the analytics team need to know? -->

### Events Being Removed

| Event Name | Current Trigger | Impact |
|------------|-----------------|--------|
| `{name}_screen_viewed` | Auto screen tracking | No longer fires |
| `{name}_item_tapped` | User interaction | No longer fires |

### Dashboard Impact

<!-- Any dashboards, alerts, or reports that reference these events? -->

- [ ] Dashboard: ...
- [ ] Alert: ...

---

## Feature Flag Cleanup

<!-- Flags that should be removed alongside the feature. -->

| Flag Key | Action |
|----------|--------|
| `{name}_enabled` | Remove — feature no longer exists |
| `{name}_v2_layout` | Remove — experiment concluded |

---

## Build Config Cleanup

<!-- Config fields that should be removed. -->

| Field | Action | Risk |
|-------|--------|------|
| `{name}BaseUrl` | Remove from Defaults.properties + all markets | Verify no other feature uses it |

---

## Test Cleanup

<!-- Tests that reference the removed code. -->

### Tests to Delete

| Test File | Module |
|-----------|--------|
| `{Feature}PresenterTest.kt` | impl/presentation |
| `{Feature}UiTest.kt` | impl/presentation |
| `{Feature}RepositoryImplTest.kt` | impl/data |
| `{Feature}UiRobot.kt` | impl/presentation |
| `{Feature}StateRobot.kt` | impl/presentation |

### Tests to Update

| Test File | Module | Change |
|-----------|--------|--------|
| `{Other}PresenterTest.kt` | features:{other} | Remove scenario that navigates to deleted screen |
| `NavigationTest.kt` | testing:navint-tests | Remove removed screen from nav assertions |
| `AppJourney.kt` | testing:e2e-tests | Remove or update journey steps |

### Fakes to Delete

| Fake | Module |
|------|--------|
| `FakeGet{Feature}Content.kt` | features:{name}:test |

---

## iOS Cleanup

<!-- SwiftUI and iOS-specific files to remove. -->

| File | Location | Purpose |
|------|----------|---------|
| `{Feature}View.swift` | iosApp/Views/ | SwiftUI screen |
| `{Feature}ViewModel.swift` | iosApp/ViewModels/ | iOS view model bridge |
| Navigation registration | iosApp/Navigation/ | Remove from nav graph |

---

## AGENTS.md Updates

<!-- Documentation that needs updating after removal. -->

- [ ] Delete `features/{name}/AGENTS.md` (if removing entire feature)
- [ ] Update `features/{other}/AGENTS.md` — remove cross-feature dependency reference
- [ ] Update root `AGENTS.md` — remove from feature list
- [ ] Update `.agents/standards/` if removal affects documented patterns

---

## Verification Checklist

<!-- After removal, verify nothing is broken. -->

- [ ] `./gradlew assemble` — full build succeeds (no unresolved references)
- [ ] `./gradlew testAndroidHostTest` — all unit tests pass
- [ ] `./gradlew :testing:architecture-check:test` — Konsist rules pass (no orphaned patterns)
- [ ] `swift test --package-path iosApp/ArchitectureCheck` — Harmonize rules pass
- [ ] `./gradlew detektMetadataCommonMain` — no unused import warnings
- [ ] `/find-dead-code` — no new dead code introduced by partial removal
- [ ] Search for removed type names — zero results across codebase

---

## Constraints & Considerations

- **Timing**: <!-- any coordination needed? feature freeze? -->
- **Data**: <!-- any persisted data that becomes orphaned? -->
- **Users**: <!-- any users currently using this feature? gradual rolldown? -->
- **Rollback**: <!-- can this removal be reverted if needed? -->
- **iOS parity**: <!-- is the iOS side also being removed? -->

---

## Out of Scope

<!-- What this removal explicitly does NOT touch. -->

- ...
- ...
