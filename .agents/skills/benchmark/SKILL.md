---
name: benchmark
description: Add, run, and analyze macrobenchmarks — Android Macrobenchmark (Perfetto) and iOS XCTest performance metrics. Scoped to authored, repeatable performance measurements against production-representative builds. Use when startup, scroll, or frame-timing regressions need regression-gated coverage.
---

# Benchmark

Add, run, and analyze macrobenchmarks for Android (`:testing:mobile:benchmarks`) and iOS (`Benchmarks` test plan → `iosAppBenchmarks` Xcode target).

**Parameters**: target (feature or scenario), platform (`android` / `ios` / both), mode (`add` / `run` / `analyze`)

**Usage examples**:
```
/benchmark order android add        # add startup/scroll benchmarks for order feature
/benchmark order android run        # run existing benchmarks, capture Perfetto traces
/benchmark startup android run      # run StartupBenchmark specifically
/benchmark order ios add            # add XCTest perf benchmarks for iOS order view
/benchmark ios run                  # run all iOS benchmarks
/benchmark android analyze          # open most recent Perfetto trace
```

## Reference

- Standard: `.agents/standards/testing-benchmarks.md`
- Android module: `testing/mobile/benchmarks/` — see `testing/mobile/benchmarks/AGENTS.md`
- iOS target: `iosAppBenchmarks` — sources in `iosApp/iosAppBenchmarks/`, test plan `Benchmarks.xctestplan`

## Key Rules (read before authoring)

- Android benchmarks target the **minified `benchmark` build type** of `:androidApp`. Do not add Compose UI test deps — `compose.ui.test.junit4` crashes the target on launch.
- Self-instrumenting is on (`android.experimental.self-instrumenting = true`). Drive the UI via `UiAutomator`, never reference target-app classes by name — R8 renames them.
- Physical Android device required; emulator is blocked. Suppress `EMULATOR` only for local smoke-tests, never in CI.
- iOS benchmarks extend `XCTestCase` with `measure(metrics:)` and run via the `Benchmarks` xcodebuild test plan against the `iosAppBenchmarks` target.

## Mode: Add

### Android — Macrobenchmark + Perfetto

Add benchmarks to `testing/mobile/benchmarks/src/main/kotlin/com/mockdonalds/app/benchmarks/`.

#### Startup Benchmark

```kotlin
@RunWith(AndroidJUnit4::class)
class {Feature}StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val targetPackage = InstrumentationRegistry.getInstrumentation().targetContext.packageName

    @Test
    fun startup() {
        benchmarkRule.measureRepeated(
            packageName = targetPackage,
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
        ) {
            pressHome()
            startActivityAndWait()
            // Drive to {feature} screen via UiAutomator if needed
        }
    }
}
```

#### Frame Timing Benchmark (scroll/animation)

```kotlin
@Test
fun scroll{Feature}List() {
    benchmarkRule.measureRepeated(
        packageName = targetPackage,
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
    ) {
        startActivityAndWait()
        device.findObject(By.desc("{feature}_list")).apply {
            setGestureMargin(device.displayWidth / 5)
            repeat(3) { scroll(Direction.DOWN, 1f) }
        }
    }
}
```

Identify elements by TestTags from `features/*/api/navigation` via `By.desc(...)` — these survive R8.

#### Custom Trace Sections

Add descriptive trace sections in production code to isolate hot paths:

```kotlin
import androidx.tracing.trace

fun loadData() = trace("{feature}_load_data") {
    // code to profile
}
```

### iOS — XCTest Performance Metrics

Add to `iosApp/iosAppBenchmarks/{Feature}PerformanceTest.swift`. File must end with `PerformanceTest` or `Benchmark` (Harmonize-enforced).

```swift
final class {Feature}PerformanceTest: XCTestCase {
    func testStartupPerformance() throws {
        measure(metrics: [XCTApplicationLaunchMetric()]) {
            XCUIApplication().launch()
        }
    }

    func testScrollPerformance() throws {
        let app = XCUIApplication()
        app.launch()
        // Navigate to feature
        measure(metrics: [XCTOSSignpostMetric.scrollDecelerationMetric]) {
            app.collectionViews.firstMatch.swipeUp(velocity: .fast)
        }
    }
}
```

## Mode: Run

### Android

```bash
# All macrobenchmarks — physical device required
./gradlew :testing:mobile:benchmarks:connectedBenchmarkAndroidTest

# Target a specific benchmark class
./gradlew :testing:mobile:benchmarks:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.mockdonalds.app.benchmarks.{Feature}Benchmark

# Market/env-specific
./gradlew :testing:mobile:benchmarks:connectedCoreIntBenchmarkAndroidTest

# Local smoke-test on emulator (noisy, never CI)
./gradlew :testing:mobile:benchmarks:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
```

Perfetto traces land in `testing/mobile/benchmarks/build/outputs/connected_android_test_additional_output/`.

### iOS

```bash
xcodebuild test -scheme iOSApp -testPlan Benchmarks \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro'
```

Performance results appear in the Xcode test report; traces via Instruments `xctrace`:

```bash
xcrun xctrace record \
  --device 'iPhone 17 Pro' \
  --template 'App Launch' \
  --output ./traces/{feature}_launch.trace \
  --launch -- /path/to/iOSApp.app
```

## Mode: Analyze

### Android — Perfetto

- Web UI: open `https://ui.perfetto.dev` and drag in the `.perfetto-trace` file
- CLI query:
  ```bash
  trace_processor --query "
    SELECT name, dur/1e6 AS duration_ms
    FROM slice
    WHERE name LIKE '%{feature}%'
    ORDER BY dur DESC LIMIT 20
  " trace.perfetto-trace
  ```

Look for: main-thread blocks > 16ms, custom trace-section durations, GC pauses, layout/measure frequency per frame.

### iOS — Instruments

- Export: `xcrun xctrace export --input trace.trace --output trace_export/`
- Open GUI: `open trace.trace`

Look for: main-thread hangs > 16ms, ARC allocation spikes, SwiftUI body re-evaluation frequency.

## Conventions

- Benchmark class names end with `Benchmark` (Android) or `PerformanceTest` / `Benchmark` (iOS)
- Android benchmarks live in `testing/mobile/benchmarks/src/main/kotlin/com/mockdonalds/app/benchmarks/`
- iOS benchmarks live in `iosApp/iosAppBenchmarks/` (dedicated Xcode target, runs via `Benchmarks.xctestplan`)
- Minimum 5 iterations for statistical significance (stock `StartupBenchmark` uses 3 — acceptable for a boilerplate)
- Custom trace sections use snake_case: `{feature}_{operation}`
- Trace files must never be committed — `*.perfetto-trace` and `*.trace` are gitignored

## Post-Change Verification

After adding a benchmark, run `verify` to compile-check the test code. Running the benchmark itself requires a physical device; plan to include it in the CI perf pipeline, not the local `verify full` run.
