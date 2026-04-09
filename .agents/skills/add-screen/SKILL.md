---
name: add-screen
description: Add a new screen to an existing feature module. Creates Screen object, TestTags, Presenter, UiState, Events, Ui composable, and full test suite (9+ files).
---

# Add Screen

Add a new screen to an existing feature with all required files.

**Parameters**: feature name, screen name, screen type (`Screen` | `ProtectedScreen` | `TabScreen`)

## Reference Standards

- Naming: `.agents/standards/naming-conventions.md`
- DI: `.agents/standards/dependency-injection.md`
- Testing: `.agents/standards/testing.md`
- Design system & adaptive layout: `.agents/standards/design-system.md`

## Reference Implementation

Use existing screens in `features/order/` as the pattern reference.

## Files to Create

| File | Location | Template |
|------|----------|----------|
| `{Screen}Screen.kt` | api/navigation/src/commonMain | Data object with @Parcelize |
| `{Screen}TestTags.kt` | api/navigation/src/commonMain (ui package) | Object with const val tags |
| `{Screen}Presenter.kt` | impl/presentation/src/commonMain | @CircuitInject + @Inject + @Composable |
| `{Screen}UiState.kt` | impl/presentation/src/commonMain | Data class : CircuitUiState + sealed Event |
| `{Screen}Ui.kt` | impl/presentation/src/androidMain | @CircuitInject Composable |
| `{Screen}PresenterTest.kt` | impl/presentation/src/commonTest | BehaviorSpec with presenterTestOf |
| `{Screen}UiTest.kt` | impl/presentation/src/androidDeviceTest | BehaviorSpec with UiRobot |
| `{Screen}UiRobot.kt` | impl/presentation/src/androidDeviceTest | Robot with StateRobot |
| `{Screen}StateRobot.kt` | impl/presentation/src/androidDeviceTest | Extends core StateRobot |

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

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes. It will:

- Detect which modules were affected by the new screen files
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch naming violations, missing annotations, DI wiring issues, and test convention problems

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
