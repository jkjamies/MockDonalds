# End-to-End Tests

## Purpose

Android instrumented test module that validates full user journeys against the **real app** — real Metro DI graph, real Circuit presenters, real data layer, real network. This is the highest test level, verifying the complete app works as users experience it.

## How It Works

- Uses `com.android.test` plugin with `targetProjectPath = ":androidApp"`
- Test APK instruments against the real app (com.mockdonalds.app)
- Tests run in a separate process using UI Automator for element access
- Benchmarks use Macrobenchmark for startup/frame timing with Perfetto traces
- No test doubles — everything is real

## What Gets Tested

| Category | Location | Tests |
|----------|----------|-------|
| Journeys | `suites/` | Full user flows: browse, order, auth gating, tab navigation |
| Deep links | `suites/` | Cold start with URI, correct screen resolution |
| Benchmarks | `benchmarks/` | Cold/warm/hot startup time via Macrobenchmark |

## Key Types

| Type | Purpose |
|------|---------|
| `AppRobot` | Top-level test helper: launch, deep link, tab nav, element assertions via UI Automator |
| `GuestJourneyTest` | Browse without auth: tabs, content, auth redirect |
| `OrderJourneyTest` | Home → order → browse featured items |
| `DeepLinkJourneyTest` | Cold start deep links: order, more, profile (auth gated) |
| `StartupBenchmark` | Cold/warm/hot startup timing via Macrobenchmark |

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
# All e2e tests (journeys + benchmarks)
./gradlew :e2e-tests:connectedAndroidTest

# Just journey tests (exclude benchmarks)
./gradlew :e2e-tests:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.notClass=com.mockdonalds.app.e2e.benchmarks.StartupBenchmark

# Just benchmarks
./gradlew :e2e-tests:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mockdonalds.app.e2e.benchmarks.StartupBenchmark
```

Requires a running Android emulator or connected device.

## Adding Tests

1. Journey tests go in `suites/` — test full user flows end-to-end
2. Benchmark tests go in `benchmarks/` — measure performance with `MacrobenchmarkRule`
3. Use `AppRobot` for all app interactions (launch, navigate, assert)
4. Use TestTags from `features/*/api/navigation` for element identification via `By.desc(tag)`
5. Journey test files must end with `JourneyTest`
6. Benchmark files must end with `Benchmark`
7. All tests use JUnit4 `@RunWith(AndroidJUnit4::class)`
8. Do NOT import from feature `test/` modules — e2e tests are fully real
