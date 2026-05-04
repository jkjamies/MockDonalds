# End-to-End Tests (consumer)

## Purpose

Android instrumented test module that validates full user journeys against the **real consumer app** — real Metro DI graph, real Circuit presenters, real data layer, real network. The highest test level, verifying the complete app works as users experience it.

> Kiosk has its own peer suite at `:testing:kiosk:e2e-tests` instrumenting against `:kioskApp`. This module's TestTags walk excludes the `features/kiosk/` subdirectory. See [`../kiosk/e2e-tests/AGENTS.md`](../kiosk/e2e-tests/AGENTS.md) and [`../../specs/kiosk-app.md`](../../specs/kiosk-app.md).

## How It Works

- Uses `com.android.test` plugin with `targetProjectPath = ":androidApp"`
- Test APK instruments against the real app (com.mockdonalds.app) **debug** variant
- Tests run in a separate process using UI Automator for element access
- No test doubles — everything is real

## What Gets Tested

| Category | Location | Tests |
|----------|----------|-------|
| Journeys | `suites/` | Full user flows: browse, order, auth gating, tab navigation |
| Deep links | `suites/` | Cold start with URI, correct screen resolution |

## Key Types

| Type | Purpose |
|------|---------|
| `AppRobot` | Top-level test helper: launch, deep link, tab nav, element assertions via UI Automator |
| `GuestJourneyTest` | Browse without auth: tabs, content, auth redirect |
| `OrderJourneyTest` | Home → order → browse featured items |
| `DeepLinkJourneyTest` | Cold start deep links: order, more, profile (auth gated) |

## Dependencies

```
e2e-tests depends on:
  :androidApp                    — target application (real everything)
  features/*/api/navigation      — TestTags for element identification

e2e-tests does NOT depend on:
  features/*/test                — no fakes (fully real)
  features/*/impl/*              — no direct code access (uses UI Automator)
  composeApp                     — instruments via :androidApp
```

## Running

```bash
# All journey tests
./gradlew :testing:e2e-tests:connectedAndroidTest
```

Requires a running Android emulator or connected device.

## Adding Tests

1. Journey tests go in `suites/` — test full user flows end-to-end
2. Use `AppRobot` for all app interactions (launch, navigate, assert)
3. Use TestTags from `features/*/api/navigation` for element identification via `By.desc(tag)`
4. Journey test files must end with `JourneyTest`
5. All tests use JUnit4 `@RunWith(AndroidJUnit4::class)`
6. Do NOT import from feature `test/` modules — e2e tests are fully real
