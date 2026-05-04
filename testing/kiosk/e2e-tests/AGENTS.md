# testing/kiosk/e2e-tests

## Purpose
End-to-end journey tests for the kiosk app. Mirrors `:testing:e2e-tests` (consumer) but instruments against `:kioskApp` instead of `:androidApp`. Driven via UI Automator + accessibility identifiers (the `*TestTags` constants from `features/kiosk/*/api/navigation/`).

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| `KioskAppRobot` | `main/.../robots/` | Top-level robot. Smaller than the consumer `AppRobot` — kiosk has no deep links and no tab nav, so the robot only exposes `launchApp()`, `assertElementDisplayed(testTag)`, `tapElement(testTag)`, `waitForElement(testTag)`. |
| `KioskFlowJourneyTest` | `main/.../suites/` | Smoke journey covering Attract → Identify (skip) → KioskOrder → back-to-Attract on a real device. Per-step / failure-path tests are a follow-up. |

## Targeting
- `targetProjectPath = ":kioskApp"` in `build.gradle.kts`.
- `missingDimensionStrategy("market", "core")`, `missingDimensionStrategy("env", "int")`.
- Runs against the kiosk app's debug variant (unminified) — production-representative R8 timing belongs in `:testing:kiosk:benchmarks`.

## Spec
Full spec: [`../../../specs/kiosk-app.md`](../../../specs/kiosk-app.md).
