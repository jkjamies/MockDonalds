# End-to-End Testing Standards

E2E tests validate full user journeys against the **real app** — real Metro DI graph, real Circuit presenters, real data layer, real network. This is the highest test level, verifying the complete app works as users experience it. No test doubles.

> Shared conventions (test stack, quality standards, fakes, infrastructure) are in [testing.md](testing.md).

## Scope

| What's tested | What's real | What's faked |
|---------------|-------------|--------------|
| Full user journeys, deep links, startup perf | Everything — DI, presenters, data, network | Nothing |

## Run Commands

```bash
# All e2e tests (journeys + benchmarks)
./gradlew :e2e-tests:connectedAndroidTest

# Journeys only (exclude benchmarks)
./gradlew :e2e-tests:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.notClass=com.mockdonalds.app.e2e.benchmarks.StartupBenchmark

# Benchmarks only
./gradlew :e2e-tests:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mockdonalds.app.e2e.benchmarks.StartupBenchmark
```

Requires a connected Android device or running emulator.

## Key Characteristics

- **Plugin**: `com.android.test` — separate test APK instruments against `:androidApp`
- **Runner**: JUnit4 `@RunWith(AndroidJUnit4::class)` — instrumented tests
- **Data layer**: Real (no fakes, no test doubles)
- **Element access**: UI Automator (`By.desc(testTag)`) — cross-process element identification
- **Benchmarks**: Macrobenchmark (`MacrobenchmarkRule`) with Perfetto traces
- **Location**: `e2e-tests/src/main/kotlin/`

## Test Organization

| Category | Location | Naming |
|----------|----------|--------|
| Journeys | `suites/` | Files end with `JourneyTest` |
| Benchmarks | `benchmarks/` | Files end with `Benchmark` |

## AppRobot

All e2e tests use `AppRobot` for app interactions:

| Method | Purpose |
|--------|---------|
| `launchApp()` | Cold launch the app |
| `launchWithDeepLink(uri)` | Cold start with a deep link URI |
| `tapTab(label)` | Navigate bottom tabs |
| `assertElementDisplayed(testTag)` | Verify element visible via `By.desc(testTag)` |
| `tapElement(testTag)` | Tap element by test tag |
| `typeText(testTag, text)` | Enter text into a field |

Element identification uses `By.desc(testTag)` where `testTag` comes from `{Feature}TestTags` in `features/*/api/navigation/`. This is the same accessibility identifier system used by UI component tests and iOS views.

## Journey Test Pattern

```kotlin
@RunWith(AndroidJUnit4::class)
class GuestJourneyTest {

    private val robot = AppRobot()

    @Before
    fun setUp() {
        robot.launchApp()
    }

    @Test
    fun homeScreenRendersOnLaunch() {
        robot.assertElementDisplayed(HomeTestTags.USER_NAME)
        robot.assertElementDisplayed(HomeTestTags.HERO_BANNER)
    }

    @Test
    fun browseAllTabs() {
        robot.assertElementDisplayed(HomeTestTags.USER_NAME)
        robot.tapTab("Order")
        robot.assertElementDisplayed(OrderTestTags.FEATURED_ITEMS_SECTION)
        // ...continue through tabs
    }
}
```

## Benchmark Pattern

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() {
        benchmarkRule.measureRepeated(
            packageName = "com.mockdonalds.app",
            metrics = listOf(StartupTimingMetric()),
            iterations = 3,
            startupMode = StartupMode.COLD,
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
```

## Module Dependencies

```
e2e-tests depends on:
  :androidApp                    — target application (real everything)
  features/*/api/navigation      — TestTags for element identification

e2e-tests does NOT depend on:
  features/*/test                — no fakes (fully real)
  features/*/impl/*              — no direct code access (uses UI Automator)
  composeApp                     — instruments via :androidApp
```

## When to Add e2e-tests

Add or update e2e-tests when:
- A new user journey is introduced (e.g., onboarding flow)
- Deep link handling is added or modified
- Tab navigation or auth gating changes
- Startup performance needs regression tracking

## Distinction from Other Test Levels

| | Unit Tests | UI Component Tests | navint-tests | e2e-tests |
|---|---|---|---|---|
| Location | `impl/*/commonTest/` | `impl/presentation/androidDeviceTest/` | `navint-tests/` | `e2e-tests/` |
| Scope | Single class | Single screen | Multi-screen flows | Full user journeys |
| Data | Fakes | Static state | Fakes | Real |
| Presenters | Isolated | Not involved | Real | Real |
| Navigation | Not involved | Not involved | Real Circuit | Real |
| Element access | N/A | Compose test | Compose test | UI Automator |
| Framework | Kotest BehaviorSpec | JUnit4 + Compose | JUnit4 + Compose | JUnit4 + UI Automator |

## Enforcement

### Konsist

4 boundary rules in `TestBoundaryTest`:
- e2e-tests must not import from feature `test/` modules (no fakes)
- e2e-tests must not import from `impl/domain` or `impl/data` (interact via UI only)
- Journey test files in `suites/` must end with `JourneyTest`
- Benchmark files in `benchmarks/` must end with `Benchmark`

1 coverage rule in `TestModuleCoverageTest`:
- Every `{Feature}TestTags` object in `api/navigation` must be referenced in at least one e2e test

### Harmonize

1 coverage rule in `TestConventionsTest.swift` (active once iOS e2e tests exist):
- Every `{Feature}View.swift` must be referenced in at least one iOS e2e journey test
