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

## Pre-flight: Grill the Spec

When the user provides a spec via `@file`, scan it for unresolved markers before scaffolding (see the grill-me skill for the full marker list): `<!-- TODO -->` placeholders, empty `- [ ]` AC items, empty required header fields, raw template placeholder prose, `...` table cells, or unconfirmed reverse-spec presumptions (`presumably` / `appears to`). If any are present, **stop and run `/grill-me @{spec}` first**, then resume this skill.

The conversion skills (`/ac-to-spec`, `/reverse-spec`) grill inline before producing output, so a marker-laden spec usually means the spec was hand-authored from a template or has gone stale. Skip the pre-flight only if the user explicitly says "skip the grill" — in that case, surface unresolved markers as `// TODO` comments in the generated code and call them out in the final summary.

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

Map the requested change to concrete file modifications across layers. The codebase is dual-platform (Android Compose / iOS SwiftUI), so an "Android only" change to a Kotlin module almost always implies a sibling SwiftUI change in `iosApp/`. Audit both platforms before declaring the layer set.

| Layer | When to touch | What changes |
|-------|---------------|--------------|
| `api/domain` | Model shape changes, new/changed use case signatures | Data classes, abstract interactors |
| `api/navigation` | New screens, changed TestTags | Screen objects, TestTags constants |
| `impl/domain` | Use case logic changes | Interactor implementations, repository interfaces |
| `impl/data` | New/changed endpoints, DTO shape changes | Repository impls, data sources, DTOs, mappers |
| `impl/presentation` (commonMain) | State changes, new events, presenter logic | Presenter, UiState, Events |
| `impl/presentation` (androidMain) | Android Compose UI changes | Compose UI |
| `impl/presentation` (iosMain) | KMP-NativeCoroutines bridge changes | iOS bridge code |
| `iosApp/iosApp/Features/{Name}/` | Per-feature SwiftUI views, view models, navigation wiring | SwiftUI views consuming the KMP presenter |
| `iosApp/iosApp/Presentation/` | Cross-cutting SwiftUI primitives (sibling to `core:presentation`) | Shared SwiftUI views — WebView, future loading/error states |
| `iosApp/iosApp/{Akamai,Circuit,Harness,Theme}/` | Cross-cutting iOS infra mirroring a Kotlin `core:` module | Swift bridges, theme, navigation glue |
| `iosApp/project.yml` | New top-level folder under `iosApp/iosApp/`, new SPM package, new test plan, target config changes | XcodeGen project spec — regenerate via `xcodegen` after edits |
| `test/` | Changed abstractions need updated fakes | Fake classes |
| `core:presentation` (`commonMain`) | New Compose extension over a Compose-free core API | `@Composable` extensions like `rememberFlag`, `collectAsState` |
| `core:presentation` (`androidMain`) | New cross-cutting Android Compose UI primitive | Standalone Composables (e.g., `WebViewContent`) — pair with iOS sibling |

#### Android ↔ iOS parity checklist

When a change touches a feature's UI or any cross-cutting presentation primitive, walk this checklist before proceeding:

- [ ] **Kotlin presenter / state / events** — `impl/presentation/commonMain` changes apply to BOTH platforms automatically. No iOS-specific work needed for state shape changes alone.
- [ ] **Android UI** — `impl/presentation/androidMain/.../{Feature}Ui.kt` (Compose).
- [ ] **iOS UI** — `iosApp/iosApp/Features/{Name}/{Feature}View.swift` (SwiftUI). Updated to consume new state fields, render new UI elements, and dispatch new events via the KMP-NativeCoroutines-bridged presenter.
- [ ] **iOS navigation glue** — `iosApp/iosApp/Circuit/` if new navigation patterns (FlowScreen, ProtectedScreen, deep links) are involved.
- [ ] **iOS test plan membership** — if new SwiftUI files need test coverage, confirm they're picked up by the relevant test plan (`UnitTests`, `UIComponentTests`, `NavIntTests`, `E2ETests`) via the targets in `iosApp/project.yml`.
- [ ] **Cross-cutting primitives** — if you added a Composable to `core:presentation/androidMain`, add the SwiftUI sibling under `iosApp/iosApp/Presentation/{Concept}/` and document the symmetric API. The two implementations should expose the same conceptual params (in their native idiom) so consumer features look symmetric.
- [ ] **`iosApp/project.yml` regeneration** — if a new top-level folder under `iosApp/iosApp/` was added, or files were added that XcodeGen doesn't auto-pick (rare; the default `path: iosApp` glob handles most cases), run `xcodegen` (or `xcodegen generate`) so `iosApp.xcodeproj/project.pbxproj` reflects the new file membership. Without this, SourceKit reports false-positive "module not found" / "type not in scope" errors and the file isn't built.
- [ ] **Android-only or iOS-only by design?** If the change is intentionally one-platform (e.g., Android-only feature flag debug surface, iOS-only widget extension), document the asymmetry in the relevant `AGENTS.md` so future contributors don't think parity is missing by accident.

### 3. Apply Changes Top-Down

Work from domain outward to maintain compilation at each step:

1. **api/domain** — update models and use case signatures
2. **impl/domain** — update use case implementations and repository interfaces
3. **impl/data** — update DTOs, mappers, data sources, repository impls
4. **api/navigation** — update TestTags if UI changes
5. **impl/presentation (commonMain)** — update UiState, Events, Presenter
6. **impl/presentation (androidMain)** — update Android Compose UI
7. **iOS SwiftUI** — update `iosApp/iosApp/Features/{Name}/` views to consume new state, render new fields, dispatch new events. Cross-cutting primitives go under `iosApp/iosApp/Presentation/`.
8. **`iosApp/project.yml`** — only edit if a new top-level folder was added under `iosApp/iosApp/`, a new SPM package was introduced, or test-plan membership changed. After editing, run `xcodegen` to regenerate `iosApp.xcodeproj`. Default path-glob membership picks up new files in existing folders without project.yml edits.
9. **test/** — update fakes to match changed abstractions
10. **Tests** — update existing tests (Kotlin Kotest unit + iOS Swift Testing unit + UI component robots on both platforms), add new test scenarios for new behavior. Each platform has its own test layer; don't skip iOS UI tests just because Android UI tests pass.

### 4. Sync Fakes and Test Defaults

Every changed abstract use case or model must have its fake updated:
- `FakeGet{Feature}Content` — update `DEFAULT` companion value
- `FakeGet{Feature}Content` — update `emit()` parameter types if model changed
- Add new fake methods if new interactor methods were added

### 5. Update AGENTS.md

If the change affects the feature's key types, cross-feature dependencies, or documented patterns, update `features/{name}/AGENTS.md` to reflect the new state.

## Key Rules

- **Never break compilation mid-change** — update types before updating consumers
- **Keep all layers in sync** — if a model field is added, it must flow through DTO → mapper → repository → use case → presenter → Android UI → **iOS SwiftUI** → tests → fakes
- **Both platforms are first-class** — every UI-touching change must be applied to BOTH the Android Compose layer (`impl/presentation/androidMain`) AND the iOS SwiftUI layer (`iosApp/iosApp/Features/{Name}/` or `iosApp/iosApp/Presentation/` for cross-cutting primitives). A "we'll do iOS later" stance silently breaks parity. If the change is intentionally one-platform, document the asymmetry in the relevant `AGENTS.md`
- **Cross-cutting primitives are split-paradigm** — Android Compose primitives belong in `core:presentation/androidMain`; the iOS SwiftUI sibling lives under `iosApp/iosApp/Presentation/{Concept}/`. APIs should be conceptually symmetric per primitive but adopt each platform's idioms (Composable + state holder vs `UIViewRepresentable` + `@Binding`). Do NOT introduce Compose-MP-iOS rendering for one-off primitives — iOS stays on SwiftUI
- **`iosApp/project.yml` is the iOS source of truth** — when adding new top-level folders under `iosApp/iosApp/`, new SPM packages, or new test-plan members, edit `project.yml` and regenerate `iosApp.xcodeproj` via `xcodegen`. SourceKit "module not found" / "type not in scope" errors on freshly-added files almost always mean the project hasn't been regenerated yet
- **Preserve existing test coverage** — update existing tests on both platforms to cover changed behavior, add new tests for new behavior
- **Don't expand scope** — only change what the spec/description asks for
- **Events stay sealed class** — not sealed interface (iOS interop)
- **Presenters use CenterPost interactors** — never call repositories directly

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify` skill to validate all changes. It will:

- Detect which modules were affected
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch naming violations, missing annotations, type mismatches, and test failures

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.

### iOS-specific verification reminders

`verify` covers Kotlin (Detekt + Kotest + Konsist + Android build). It does NOT exhaustively cover iOS — make sure the iOS side is also verified when iOS files were touched:

- **SwiftLint** runs as part of `verify`, but the Swift compile + Xcode build do not. Run `xcodebuild build -scheme iOSApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro'` (or use the `mcp__xcode__BuildProject` tool) to confirm the iOS app still compiles after SwiftUI edits
- **iOS unit tests** — if behavior changes touched logic exercised by `iosAppTests/Unit/`, run `xcodebuild test … -testPlan UnitTests`
- **iOS UI component tests** — if SwiftUI views changed, run `xcodebuild test … -testPlan UIComponentTests`
- **Harmonize architecture tests** — `swift test --package-path iosApp/ArchitectureCheck` covers iOS-side conventions; run when iOS folder structure or SwiftUI organization changed
- **`xcodegen` regeneration** — if `iosApp/project.yml` was edited or new top-level folders/files were added, the project.pbxproj must be regenerated. Without this, the new files will not compile and Xcode will report stale errors
