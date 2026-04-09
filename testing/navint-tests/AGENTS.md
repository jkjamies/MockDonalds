# Navigation + Integration Tests

## Purpose

Android instrumented test module that validates cross-feature navigation and state propagation with **real Circuit presenters and fake data layer**. This is the middle layer between unit tests (isolated, fast) and E2E tests (full real app).

## How It Works

- **NavIntAppGraph** extends `AppGraph` (from `core:metro`) with `@DependencyGraph`
- Metro generates a DI graph where:
  - `api/domain`, `api/navigation`, `impl/presentation` — real (same as production)
  - `impl/domain`, `impl/data` — **not on classpath** (excluded from dependencies)
  - `test/` modules — fakes with `@ContributesBinding` are the **sole bindings** for abstract use cases
- `TestApplication` creates the graph; `TestRunner` wires it as the instrumentation app
- Tests use `setNavIntContent()` helper to set up full Circuit navigation with real theme and window size

## What Gets Tested

| Category | Location | Tests |
|----------|----------|-------|
| Navigation | `navigation/` | Screen rendering with real presenters, tab switching, screen transitions |
| Integration | `integration/` | Auth state propagation, cross-feature navigation with auth gating |

## Key Types

| Type | Purpose |
|------|---------|
| `NavIntAppGraph` | `@DependencyGraph` — Metro generates bindings from real presenters + fake data |
| `TestApplication` | Creates and holds the `NavIntAppGraph` instance |
| `TestRunner` | `AndroidJUnitRunner` subclass that installs `TestApplication` |
| `TestAuthProvider` | `@ContributesTo` interface providing `FakeAuthManager` as `AuthManager` binding |
| `setNavIntContent()` | Extension on `ComposeContentTestRule` — sets up Circuit + theme + navigation |

## Dependencies

```
navint-tests depends on:
  features/*/api/domain        — models, abstract use cases
  features/*/api/navigation    — Screen objects, TestTags
  features/*/impl/presentation — real presenters + Compose UI
  features/*/test              — fakes (@ContributesBinding, sole data bindings)
  core/metro                   — AppGraph interface
  core/circuit                 — CircuitProviders, TabScreen, ProtectedScreen
  core/auth/api                — AuthManager interface
  core/centerpost              — CenterPost interactors
  core/theme                   — MockDonaldsTheme, design tokens
  core/test-fixtures           — FakeAuthManager, TestCenterPostDispatchers

navint-tests does NOT depend on:
  features/*/impl/domain       — real use case implementations
  features/*/impl/data         — real repositories, DTOs, network
  core/auth/impl               — real auth implementation
  core/network                 — real HTTP client
  composeApp                   — app shell, bottom navigation
```

## Running

```bash
./gradlew :testing:navint-tests:connectedAndroidDeviceTest
```

Requires a running Android emulator or connected device.

## Adding Tests

1. Navigation tests go in `navigation/` — test screen rendering and transitions
2. Integration tests go in `integration/` — test cross-feature state and behavior
3. Use `setNavIntContent()` to set up the Circuit environment
4. Access the graph via `(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApplication).graph`
5. Use TestTags from `features/*/api/navigation` for assertions
6. Use JUnit4 `@RunWith(AndroidJUnit4::class)` (not Kotest — instrumented tests require JUnit runner)
