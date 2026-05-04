# testing/kiosk/navint-tests

## Purpose
Navigation + integration tests for the kiosk surface. Mirrors `:testing:navint-tests` (consumer) but auto-discovers `features/kiosk/*` instead, plus the reused `features/order` test fakes. Real Circuit presenters with fake data layer — no network, no real backend.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| `KioskNavIntAppGraph` | `commonMain` | `@DependencyGraph(AppScope::class) interface : AppGraph` — wires `core/*` real bindings + kiosk feature impl/presentation (real presenters) + features/order test fakes + features/kiosk/{*}/test fakes. |
| `KioskTestApplication` | `androidDeviceTest` | `Application` subclass exposing the graph via `createGraph<KioskNavIntAppGraph>()`. |
| `KioskTestRunner` | `androidDeviceTest` | `AndroidJUnitRunner` subclass that points instrumentation at `KioskTestApplication`. Wired in `build.gradle.kts` via `withDeviceTest { instrumentationRunner = "..." }`. |
| `KioskTestAuthProvider` | `commonMain` | `@ContributesTo(AppScope::class)` interface with a `@Provides` method returning `FakeAuthManager()` from `core:test-fixtures`. Mirrors the consumer `TestAuthProvider`. |
| `setKioskNavIntContent` | `androidDeviceTest` | Compose-rule extension that wraps content in `MockDonaldsTheme` + provides `LocalWindowSizeClass` sized for kiosk hardware (540×960dp). |

## Test Suites
- `journeys/KioskFlowNavigationTest` — combined Attract → Identify → KioskOrder → back-to-Attract smoke journey + per-screen render assertions.

Per-transition isolation tests are a follow-up.

## Spec
Full spec: [`../../../specs/kiosk-app.md`](../../../specs/kiosk-app.md).
