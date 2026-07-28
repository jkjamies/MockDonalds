---
name: run-e2e-tests
description: Run Android and iOS end-to-end journey tests. Android uses `:testing:e2e-tests` against the debug target with UI Automator. iOS uses the E2ETests test plan via xcodebuild. Requires a connected device/emulator (Android) or simulator (iOS). Use after modifying navigation, deep links, auth gating, or full-flow behavior.
---

# Run E2E Tests

Run journey tests that exercise the full app end-to-end — real DI, real presenters, real data layer, real network.

## When to Use

- After modifying navigation, deep links, auth gating, or cross-feature flows
- Before opening a PR that touches `:androidApp`, `:composeApp`, or any `api/navigation`
- After changing market/env flavor resolution at the app level

## Android Journey Tests

### Prerequisites

A connected Android device or running emulator.

### Run All Android E2E Tests

```bash
./gradlew :testing:e2e-tests:connectedAndroidTest
```

Runs against the debug target (unminified, full Compose UI test deps available). Emulator is fine; benchmarks are not in this module.

### Market/Env Variants

```bash
./gradlew :testing:e2e-tests:connectedCoreIntDebugAndroidTest   # core market, int env
./gradlew :testing:e2e-tests:connectedUsDevDebugAndroidTest     # us market, dev env
```

### Run a Specific Suite

```bash
./gradlew :testing:e2e-tests:connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.jkjamies.sampleplatter.e2e.suites.GuestJourneyTest
```

### Test Structure

- Journey tests live in `testing/e2e-tests/src/main/kotlin/com/jkjamies/sampleplatter/e2e/suites/`
- Files end with `JourneyTest` (Konsist-enforced)
- Use `@RunWith(AndroidJUnit4::class)` and `AppRobot` for all interactions
- Element access via `By.desc(testTag)` — TestTags imported from `features/*/api/navigation`
- No fakes, no direct code access to `impl/*` — UI Automator only

### Interpreting Results

- Output: JUnit4 test name + UI Automator assertion detail
- Reports: `testing/e2e-tests/build/reports/androidTests/connected/`
- Common failure modes: deep link resolution broken, TestTag renamed without updating the journey, auth gating regression, tab navigation state lost

## iOS Journey Tests

### Prerequisites

An iOS Simulator (e.g., iPhone 17 Pro).

### Run All iOS E2E Tests

```bash
xcodebuild test -scheme iOSApp -testPlan E2ETests \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro'
```

The `E2ETests` test plan covers journey suites only (`Suites/`). Performance benchmarks live in the separate `iosAppBenchmarks` target and run via the `Benchmarks` test plan — see `/benchmark`.

### Test Structure

- Journeys live in `iosApp/iosAppE2ETests/Suites/`
- Files end with `JourneyTest` (Harmonize-enforced)
- Extend `XCTestCase` — XCUITest process isolation, no ViewInspector
- Use `AppRobot` (`iosApp/iosAppE2ETests/Robots/AppRobot.swift`) for all interactions
- Accessibility identifiers are raw string constants matching KMP TestTags (process-isolated, cannot import from KMP)

### Interpreting Results

- Output: XCTest test name + XCUIElement assertion detail
- Reports: Xcode test navigator or `.xcresult` bundles
- Common failure modes: accessibility identifier drift between Swift constants and KMP TestTags, tab bar layout shift, auth-gated screen not presented as `.fullScreenCover`

## Standard Reference

- Android + iOS journey conventions: `.agents/standards/testing-e2e.md`
- Android module: `testing/e2e-tests/AGENTS.md`
- Related skills: `/run-ui-tests` (single-screen UI component tests), `/run-all-tests` (full pipeline)
