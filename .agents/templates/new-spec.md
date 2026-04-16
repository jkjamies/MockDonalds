# New — Spec Template

> Copy this template, fill in the sections relevant to your work, and delete sections
> that don't apply. Feed it to any `add-*` skill via `@file` or paste inline.
>
> Usage: `/add-feature deals @specs/deals.md`

---

## Overview

<!-- One-paragraph summary of what's being built and why it exists. -->

**Name**: <!-- lowercase identifier, e.g., deals -->
**Primary screen**: <!-- PascalCase, e.g., Deals -->
**Skill target**: <!-- add-feature | add-screen | add-use-case | add-repository | add-core-module -->

---

## Business Context

<!-- What user problem does this solve? What's the user-facing workflow?
     This feeds directly into the feature's AGENTS.md business context section. -->

**User story**: As a [role], I want to [action] so that [outcome].

**Acceptance criteria**:
- [ ] <!-- observable behavior, not implementation detail -->
- [ ] 
- [ ] 

---

## Domain Models

<!-- Define the data shapes that flow through the feature. These populate
     api/domain model classes, DTOs, mappers, fake defaults, and test assertions. -->

### Primary Model

```
{Feature}Content
  ├── id: String
  ├── title: String
  ├── description: String
  ├── imageUrl: String
  └── ...
```

### Supporting Models (if any)

```
{Sub}Item
  ├── ...
  └── ...
```

### Enums / Sealed Types (if any)

```
{Feature}Status
  ├── Active
  ├── Expired
  └── ...
```

---

## API / Network

<!-- Define the endpoint(s) this feature calls. Populates RemoteDataSource,
     RemoteDataSourceImpl, DTOs, and HttpClient configuration. -->

**Base URL config field**: <!-- e.g., dealsBaseUrl — must exist in AppBuildConfig or use add-config-field skill first -->

### Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/{resource}` | required / optional / none | Fetch all items |
| POST | `/v1/{resource}` | required | Submit new item |

### Request Body (if applicable)

```json
{
  "field": "value"
}
```

### Response Shape

<!-- Maps directly to the DTO. Field names here become @Serializable data class fields. -->

```json
{
  "id": "abc-123",
  "title": "Free Fries Friday",
  "description": "Get free fries with any purchase",
  "image_url": "https://cdn.example.com/deals/fries.png",
  "expires_at": "2026-05-01T00:00:00Z"
}
```

### Error Responses (if non-standard)

| Status | Body | Handling |
|--------|------|----------|
| 404 | `{ "error": "not_found" }` | Show empty state |
| 429 | — | Retry with backoff |

---

## Use Cases

<!-- Each use case becomes an abstract class in api/domain and an impl in impl/domain.
     Specify params, result type, and data flow. -->

| Name | Type | Params | Result | Description |
|------|------|--------|--------|-------------|
| `Get{Feature}Content` | `CenterPostSubjectInteractor` (streaming) | `Unit` | `{Feature}Content` | Observe feature data |
| `Submit{Action}` | `CenterPostInteractor` (one-shot) | `{Params}` | `Result<Unit>` | Perform action |

### Data Flow

<!-- How do use cases combine repository data? e.g.:
     GetDealsContent combines dealsRepository.getDeals() + userRepository.getFavorites()
     via combine { deals, favorites -> DealsContent(...) } -->

```
Get{Feature}Content
  └── repository.get{Feature}()  →  Flow<{Feature}Content>
```

---

## Repository

<!-- Interface in impl/domain, implementation in impl/data.
     List the methods and their return types. -->

| Method | Return Type | Description |
|--------|-------------|-------------|
| `get{Feature}()` | `Flow<{DataType}>` | Stream feature data |
| `submit{Action}(params)` | `suspend Result<Unit>` | One-shot operation |

### Data Sources

<!-- Which data sources back this repository? -->

- [ ] **Remote** — REST API via HttpClientFactory
- [ ] **Local** — Room/DataStore for caching or offline
- [ ] **Combined** — remote-first with local cache

---

## Screen & UI

<!-- Populates Screen object, Presenter, UiState, Events, and Compose UI. -->

**Screen type**: <!-- Screen | ProtectedScreen | TabScreen | FlowScreen -->
**Tab tag** (if TabScreen): <!-- e.g., "deals" -->
**Auth gated** (if ProtectedScreen): <!-- yes/no -->

### UI States

| State | Fields | Description |
|-------|--------|-------------|
| Loading | — | Skeleton / shimmer |
| Success | `items: List<Item>`, `selectedId: String?` | Main content |
| Error | `message: String`, `retry: () -> Unit` | Error with retry |
| Empty | — | No items available |

### Events

<!-- Sealed class members. Each becomes a case in the presenter's event handler. -->

| Event | Fields | Behavior |
|-------|--------|----------|
| `OnItemTapped` | `id: String` | Navigate to detail screen |
| `OnRefresh` | — | Re-fetch data |
| `OnBackTapped` | — | Pop navigation |

### UI Description

<!-- Describe the layout, key components, and any adaptive/responsive behavior.
     Reference design-system.md patterns where applicable. -->

- Top: ...
- Content: ...
- Bottom: ...
- Adaptive behavior (compact vs expanded): ...

### Test Tags

<!-- Constants for UI testing. Become {Feature}TestTags object entries. -->

| Tag | Constant | Target |
|-----|----------|--------|
| `{name}_screen` | `SCREEN` | Root container |
| `{name}_list` | `LIST` | Main list |
| `{name}_item_{id}` | `item(id)` | Individual item |
| `{name}_error` | `ERROR` | Error state |

---

## Navigation

<!-- How does this screen fit into the app's navigation graph? -->

**Entry points** (how users get here):
- Bottom tab / menu item / deep link / other screen navigates here

**Outgoing navigation** (where this screen navigates to):
- `{Other}Screen` — on item tap
- `Navigator.pop()` — on back

**Deep link** (if applicable): `mockdonalds:///{path}`

---

## Analytics

<!-- Events to track. Become sealed class entries extending AnalyticsEvent. -->

| Event Name | Trigger | Properties |
|------------|---------|------------|
| `{name}_screen_viewed` | Automatic (screen tracking) | — |
| `{name}_item_tapped` | User taps item | `item_id`, `position` |
| `{name}_refresh` | Pull to refresh | — |

---

## Feature Flags

<!-- Flags that gate behavior. Defined as FeatureFlag instances in api/domain. -->

| Flag Key | Default | Description |
|----------|---------|-------------|
| `{name}_enabled` | `false` | Gates entire feature visibility |
| `{name}_v2_layout` | `false` | Enables redesigned layout |

---

## Build Config

<!-- New config fields needed. Each requires the add-config-field skill. -->

| Field | Default | Per-Market | Description |
|-------|---------|------------|-------------|
| `{name}BaseUrl` | `https://api.mockdonalds.com/{name}` | yes | API base URL |

---

## Cross-Feature Dependencies

<!-- What other features does this depend on or interact with? -->

**Imports from** (api/ modules this feature uses):
- `core:auth` — for authenticated requests
- `features:other:api:domain` — for shared models

**Imported by** (other features that will use this feature's api/):
- `features:home` — shows preview on home screen

---

## Testing Notes

<!-- Anything the generated tests should specifically cover beyond the standard patterns. -->

- Edge case: ...
- Race condition concern: ...
- Market-specific behavior: ...
