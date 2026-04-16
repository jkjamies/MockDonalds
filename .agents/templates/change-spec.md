# Change — Spec Template

> Copy this template, fill in the sections relevant to your change, and delete sections
> that don't apply. Feed it to any skill via `@file` or paste inline.
>
> Usage: `/update order @specs/order-cart-summary.md`

---

## Overview

<!-- One-paragraph summary: what's changing and why. -->

**Feature**: <!-- which feature(s) are affected, e.g., order -->
**Change type**: <!-- enhancement | bugfix | refactor | performance | accessibility | localization -->
**Scope**: <!-- narrow (1-2 files) | moderate (one layer) | broad (cross-layer) | cross-feature -->

---

## Current Behavior

<!-- Describe what happens today. Be specific — reference screens, states, user actions.
     This grounds the change and prevents misunderstanding. -->

**What the user sees**:

**What the code does**:
<!-- e.g., OrderPresenter collects GetOrderContent flow, maps to UiState with categories + cart summary.
     Currently no empty state handling — if categories list is empty, shows blank screen. -->

**Relevant files** (optional — skill will find them, but this speeds things up):
- `features/{name}/impl/presentation/{Feature}Presenter.kt`
- `features/{name}/impl/presentation/{Feature}UiState.kt`
- ...

---

## Desired Behavior

<!-- Describe the target state. Same structure as above — what the user should see,
     what the code should do. Be explicit about the delta. -->

**What the user should see**:

**What the code should do**:

---

## Affected Layers

<!-- Check which layers need changes. This helps scope the work and ensures nothing is missed. -->

- [ ] **api/domain** — new/changed models or use case signatures
- [ ] **api/navigation** — new/changed Screen objects or TestTags
- [ ] **impl/domain** — use case logic changes
- [ ] **impl/data** — repository, data source, DTO, or mapper changes
- [ ] **impl/presentation (common)** — presenter logic, UiState, events
- [ ] **impl/presentation (androidMain)** — Compose UI changes
- [ ] **impl/presentation (iosMain)** — KMP-NativeCoroutines bridge changes
- [ ] **test/** — fake updates needed for changed abstractions
- [ ] **core module(s)** — specify: ...
- [ ] **build-config** — new/changed config fields
- [ ] **iosApp (SwiftUI)** — SwiftUI views, navigation, view models
- [ ] **composeApp** — Android app-level wiring

---

## Domain Model Changes

<!-- Only fill if models are changing. Show the diff conceptually. -->

### Added Fields

```
{Feature}Content
  + newField: Type              // why this field exists
  + anotherField: Type?         // nullable because ...
```

### Changed Fields

```
{Feature}Content
  ~ oldFieldName → newFieldName: Type    // renamed because ...
  ~ fieldName: OldType → NewType         // type changed because ...
```

### Removed Fields

```
{Feature}Content
  - deprecatedField: Type       // removed because ...
```

### New Models

```
{New}Model
  ├── field: Type
  └── ...
```

---

## API / Network Changes

<!-- Only fill if endpoints, DTOs, or HTTP config are changing. -->

### New Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/{resource}/{action}` | required | New action |

### Changed Endpoints

| Endpoint | Change | Reason |
|----------|--------|--------|
| `GET /v1/{resource}` | Added `?include=extras` query param | Need additional data for new UI |

### DTO Changes

```
{Feature}Dto
  + newField: String            // maps to domain model's newField
  ~ old_name → new_name: String // API contract changed
```

### Mapper Changes

<!-- Describe any non-obvious mapping logic that changed. -->

---

## Use Case Changes

<!-- Only fill if use case logic is changing. -->

| Use Case | Change | Details |
|----------|--------|---------|
| `Get{Feature}Content` | Modified | Now combines additional repository flow |
| `Submit{Action}` | New | One-shot interactor for new user action |

### Updated Data Flow

```
Get{Feature}Content
  ├── repository.get{Feature}()
  ├── repository.getExtras()        ← NEW
  └── combine { content, extras ->
        content.copy(extras = extras)  ← NEW
      }
```

---

## UI / Presenter Changes

<!-- Only fill if presentation layer is changing. -->

### UiState Changes

```
{Feature}UiState
  + extras: List<Extra>         // new section in UI
  + isRefreshing: Boolean       // pull-to-refresh state
```

### New Events

| Event | Fields | Behavior |
|-------|--------|----------|
| `OnExtraTapped` | `id: String` | Navigate to extra detail |

### Changed Events

| Event | Change | Reason |
|-------|--------|--------|
| `OnItemTapped` | Now also triggers analytics | Adding tracking |

### UI Layout Changes

<!-- Describe what visually changes. Reference design-system.md patterns. -->

---

## Navigation Changes

<!-- Only fill if navigation flow is changing. -->

| Change | From | To |
|--------|------|----|
| New destination | — | `{Extra}DetailScreen` on extra tap |
| Auth gating | `Screen` | `ProtectedScreen` — now requires login |
| Flow change | Direct push | FlowScreen wrapper for multi-step |

---

## Analytics Changes

<!-- Only fill if analytics events are being added/changed/removed. -->

### New Events

| Event Name | Trigger | Properties |
|------------|---------|------------|
| `{name}_extra_tapped` | Tap on extra item | `extra_id`, `position` |

### Changed Events

| Event Name | Change |
|------------|--------|
| `{name}_item_tapped` | Added `source` property |

### Removed Events

| Event Name | Reason |
|------------|--------|
| `{name}_legacy_tap` | Replaced by `{name}_item_tapped` |

---

## Feature Flag Changes

<!-- Only fill if feature flags are being added/changed/removed. -->

| Flag Key | Change | Description |
|----------|--------|-------------|
| `{name}_extras_enabled` | New | Gates the new extras section |
| `{name}_old_layout` | Remove | No longer needed after migration |

---

## Build Config Changes

<!-- Only fill if config fields are changing. Requires add-config-field skill. -->

| Field | Change | Details |
|-------|--------|---------|
| `{name}ExtrasBaseUrl` | New | Separate service for extras data |

---

## Test Impact

<!-- What tests need to change? What new test scenarios exist? -->

### New Test Scenarios

- [ ] Presenter handles extras loading + empty extras list
- [ ] UI renders extras section when present, hides when empty
- [ ] Repository combines remote extras with existing data
- [ ] Fake updated with extras support

### Changed Tests

- [ ] `{Feature}PresenterTest` — add extras scenarios
- [ ] `{Feature}UiTest` — add extras UI assertions
- [ ] `{Feature}StateRobot` — add extras state configuration

### Fakes to Update

- [ ] `FakeGet{Feature}Content` — DEFAULT needs extras field

---

## Constraints & Considerations

<!-- Anything the implementer should watch out for. -->

- **Backward compatibility**: ...
- **Market-specific behavior**: ...
- **Performance concern**: ...
- **iOS parity**: Does this change need SwiftUI updates?
- **Accessibility**: ...

---

## Out of Scope

<!-- Explicitly state what this change does NOT include to prevent scope creep. -->

- ...
- ...
