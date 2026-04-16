---
name: update
description: Modify existing feature code across all affected layers — domain models, data, presentation, tests, and fakes. Use when changing behavior, adding fields, or enhancing existing features.
---

# Update

Apply a change to existing feature code, keeping all layers in sync.

**Parameters**: feature name (optional if spec provides it)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — just the feature name and a short description of the change.
2. **`@file` reference** — e.g., `/update @specs/order-cart-total.md`. The CLI resolves the file and includes its content. Extract feature name, affected layers, model changes, UI changes, and test impact from the spec. Template: `.agents/templates/change-spec.md`.
3. **Inline description** — free text describing the change. Extract whatever is provided and apply it across all affected layers.

## Reference Standards

- Architecture & module structure: `.agents/standards/architecture.md`
- Naming conventions: `.agents/standards/naming-conventions.md`
- DI patterns: `.agents/standards/dependency-injection.md`
- Testing conventions: `.agents/standards/testing.md`
- Unit tests: `.agents/standards/testing-unit.md`
- UI component tests: `.agents/standards/testing-ui-component.md`
- CenterPost interactors: `.agents/standards/centerpost.md`
- Design system & adaptive layout: `.agents/standards/design-system.md`

## Steps

### 1. Understand the Current State

- Read the feature's `AGENTS.md` for business context and key types
- Read the specific files being changed to understand current behavior
- Identify all layers affected by the change

### 2. Plan the Change

Map the requested change to concrete file modifications across layers:

| Layer | When to touch | What changes |
|-------|---------------|--------------|
| `api/domain` | Model shape changes, new/changed use case signatures | Data classes, abstract interactors |
| `api/navigation` | New screens, changed TestTags | Screen objects, TestTags constants |
| `impl/domain` | Use case logic changes | Interactor implementations, repository interfaces |
| `impl/data` | New/changed endpoints, DTO shape changes | Repository impls, data sources, DTOs, mappers |
| `impl/presentation` (commonMain) | State changes, new events, presenter logic | Presenter, UiState, Events |
| `impl/presentation` (androidMain) | UI layout changes | Compose UI |
| `impl/presentation` (iosMain) | KMP-NativeCoroutines bridge changes | iOS bridge code |
| `iosApp` | SwiftUI-specific changes | SwiftUI views |
| `test/` | Changed abstractions need updated fakes | Fake classes |

### 3. Apply Changes Top-Down

Work from domain outward to maintain compilation at each step:

1. **api/domain** — update models and use case signatures
2. **impl/domain** — update use case implementations and repository interfaces
3. **impl/data** — update DTOs, mappers, data sources, repository impls
4. **api/navigation** — update TestTags if UI changes
5. **impl/presentation** — update UiState, Events, Presenter, UI
6. **test/** — update fakes to match changed abstractions
7. **Tests** — update existing tests, add new test scenarios for new behavior

### 4. Sync Fakes and Test Defaults

Every changed abstract use case or model must have its fake updated:
- `FakeGet{Feature}Content` — update `DEFAULT` companion value
- `FakeGet{Feature}Content` — update `emit()` parameter types if model changed
- Add new fake methods if new interactor methods were added

### 5. Update AGENTS.md

If the change affects the feature's key types, cross-feature dependencies, or documented patterns, update `features/{name}/AGENTS.md` to reflect the new state.

## Key Rules

- **Never break compilation mid-change** — update types before updating consumers
- **Keep all layers in sync** — if a model field is added, it must flow through DTO → mapper → repository → use case → presenter → UI → tests → fakes
- **Preserve existing test coverage** — update existing tests to cover changed behavior, add new tests for new behavior
- **Don't expand scope** — only change what the spec/description asks for
- **Events stay sealed class** — not sealed interface (iOS interop)
- **Presenters use CenterPost interactors** — never call repositories directly

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes. It will:

- Detect which modules were affected
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch naming violations, missing annotations, type mismatches, and test failures

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
