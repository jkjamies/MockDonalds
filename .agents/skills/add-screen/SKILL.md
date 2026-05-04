---
name: add-screen
description: Add a new screen to an existing feature module. Creates Screen object, TestTags, Presenter, UiState, Events, Ui composable, and full test suite (9+ files).
---

# Add Screen

Add a new screen to an existing feature with all required files.

**Parameters**: feature name, screen name, screen type (`Screen` | `ProtectedScreen` | `TabScreen`)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — just the feature and screen name. Scaffold with placeholders.
2. **`@file` reference** — e.g., `/add-screen @specs/checkout.md`. The CLI resolves the file and includes its content. Use it to populate UiState fields, events, presenter logic, UI layout, test tags, and test assertions instead of using placeholders. If no feature/screen name is provided as arguments, extract them from the spec's Overview section.
3. **Inline description** — free text typed after the parameters (or on its own). Extract whatever is provided (feature name, screen name, state fields, events, UI description, navigation targets) and use it the same way as a spec file.

When context is provided, replace placeholders with real values everywhere: UiState, events, presenter logic, UI composable, test tags, and test assertions. If context is partial, fill in what you can and leave `// TODO` only for genuinely unknown parts.

Templates are available in `.agents/templates/new-spec.md` for structured input.

## Pre-flight: Grill the Spec

When the user provides a spec via `@file`, scan it for unresolved markers before scaffolding (see the grill-me skill for the full marker list): `<!-- TODO -->` placeholders, empty `- [ ]` AC items, empty required header fields, raw template placeholder prose, `...` table cells, or unconfirmed reverse-spec presumptions (`presumably` / `appears to`). If any are present, **stop and run `/grill-me @{spec}` first**, then resume this skill.

The conversion skills (`/ac-to-spec`, `/reverse-spec`) grill inline before producing output, so a marker-laden spec usually means the spec was hand-authored from a template or has gone stale. Skip the pre-flight only if the user explicitly says "skip the grill" — in that case, surface unresolved markers as `// TODO` comments in the generated code and call them out in the final summary.

## Reference Standards

- Naming: `.agents/standards/naming-conventions.md`
- DI: `.agents/standards/dependency-injection.md`
- Unit tests: `.agents/standards/testing-unit.md`
- UI component tests: `.agents/standards/testing-ui-component.md`
- Design system & adaptive layout: `.agents/standards/design-system.md`

## Reference Implementation

Use existing screens in `features/mobile/home/` as the pattern reference.

## Files to Create

| File | Location | Template |
|------|----------|----------|
| `{Screen}Screen.kt` | api/navigation/src/commonMain | Data object with @Parcelize |
| `{Screen}TestTags.kt` | api/navigation/src/commonMain (ui package) | Object with const val tags |
| `{Screen}Presenter.kt` | impl/presentation/src/commonMain | @CircuitInject + @Inject + @Composable |
| `{Screen}UiState.kt` | impl/presentation/src/commonMain | Data class : CircuitUiState + sealed Event |
| `{Screen}Ui.kt` | impl/presentation/src/androidMain | @CircuitInject Composable |
| `{Screen}View.swift` | iosApp/iosApp/Features/{Feature}/ | SwiftUI View with `@CircuitInject({Screen}.self, {Screen}UiState.self)` (drives factory codegen — no AppDelegate edit) |
| `{Screen}PresenterTest.kt` | impl/presentation/src/commonTest | BehaviorSpec with presenterTestOf |
| `{Screen}UiTest.kt` | impl/presentation/src/androidDeviceTest | BehaviorSpec with UiRobot |
| `{Screen}UiRobot.kt` | impl/presentation/src/androidDeviceTest | Robot with StateRobot |
| `{Screen}StateRobot.kt` | impl/presentation/src/androidDeviceTest | Extends core StateRobot |
| `{Screen}ViewTest.swift` | iosApp/iosAppTests/UIComponent/{Feature}/ | Swift Testing + ViewInspector Robot pattern |
| `{Screen}ViewRobot.swift` | iosApp/iosAppTests/UIComponent/{Feature}/ | `@MainActor final class` with ViewInspector |
| `{Screen}StateRobot.swift` | iosApp/iosAppTests/UIComponent/{Feature}/ | Extends `BaseStateRobot` |

## Screen Type Selection

- `Screen` — standard navigation target (most common)
- `ProtectedScreen` — requires authentication, triggers `AuthInterceptor` redirect to login
- `TabScreen` — bottom navigation tab, needs `override val tag: String`

## Key Conventions

- Screen objects are data objects (not classes) with `@Parcelize`
- Presenters must have `@CircuitInject({Screen}::class, AppScope::class)`, `@Inject`, and `@Composable`
- UiState must implement `CircuitUiState` and include `eventSink: ({Event}) -> Unit`
- Events must be `sealed class` (not `sealed interface`) for iOS interop
- Ui functions must have `@CircuitInject({Screen}::class, AppScope::class)` and `@Composable`
- SwiftUI Views must `import CircuitMacros` and carry `@CircuitInject({Screen}.self, {Screen}UiState.self)` — `CircuitFactoryRegistry` codegen picks it up at build time, so `AppDelegate.swift` never needs editing. See `.agents/standards/ios-interop.md`.

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify` skill to validate all changes. It will:

- Detect which modules were affected by the new screen files
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch naming violations, missing annotations, DI wiring issues, and test convention problems

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
