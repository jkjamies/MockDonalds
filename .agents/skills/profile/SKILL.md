---
name: profile
description: Run and analyze performance profiling — Perfetto/Macrobenchmark traces (Android) and Instruments traces (iOS). Use for adding benchmarks to e2e tests, running profiling sessions, and analyzing trace results.
---

# Profile

Run performance profiling and benchmarking for Android (Perfetto/Macrobenchmark) and iOS (Instruments).

**Parameters**: target (feature or scenario), platform (`android` or `ios` or both), mode (`add` to add benchmarks, `run` to execute, `analyze` to review traces)

**Usage examples**:
```
/profile order android add         # add startup/scroll benchmarks for order feature
/profile order android run         # run existing benchmarks, capture traces
/profile order ios run             # run Instruments profiling on iOS
/profile android run startup       # run startup benchmark specifically
/profile android analyze           # analyze most recent trace
```

## Reference

- E2E test standard: `.agents/standards/testing-e2e.md`
- Existing benchmarks: `testing/e2e-tests/src/androidTest/.../benchmarks/`
- Existing benchmark commands from `CLAUDE.md`:
  ```bash
  # Android benchmarks
  ./gradlew :testing:e2e-tests:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.mockdonalds.app.e2e.benchmarks.StartupBenchmark

  # iOS benchmarks
  xcodebuild test -scheme iOSApp -testPlan E2ETests -destination 'platform=iOS Simulator,name=iPhone 16'
  ```

## Mode: Add Benchmarks

### Android — Macrobenchmark + Perfetto

Add benchmark tests to `testing/e2e-tests/`:

#### Startup Benchmark

```kotlin
@RunWith(AndroidJUnit4::class)
class {Feature}StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startup() {
        benchmarkRule.measureRepeated(
            packageName = "com.mockdonalds.app",
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
        ) {
            pressHome()
            startActivityAndWait()
            // Navigate to feature screen
        }
    }
}
```

#### Frame Timing Benchmark (scroll/animation)

```kotlin
@Test
fun scroll{Feature}List() {
    benchmarkRule.measureRepeated(
        packageName = "com.mockdonalds.app",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
    ) {
        startActivityAndWait()
        // Navigate to feature, then scroll
        device.findObject(By.res("{feature}_list")).apply {
            setGestureMargin(device.displayWidth / 5)
            repeat(3) { scroll(Direction.DOWN, 1f) }
        }
    }
}
```

#### Custom Trace Sections

Add custom trace sections to production code for profiling:

```kotlin
import androidx.tracing.trace

// In presenter or repository
fun loadData() = trace("{feature}_load_data") {
    // code to profile
}
```

### iOS — XCTest Performance Metrics

```swift
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
        let list = app.collectionViews.firstMatch
        list.swipeUp(velocity: .fast)
    }
}
```

## Mode: Run Profiling

### Android — Perfetto

```bash
# Run Macrobenchmark (generates Perfetto trace automatically)
./gradlew :testing:e2e-tests:connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.mockdonalds.app.e2e.benchmarks.{Feature}Benchmark

# Manual Perfetto capture (requires connected device)
adb shell perfetto \
  -c - --txt \
  -o /data/misc/perfetto-traces/trace.perfetto-trace \
  <<EOF
buffers: { size_kb: 63488, fill_policy: RING_BUFFER }
data_sources: { config { name: "linux.ftrace" ftrace_config {
  ftrace_events: "sched/sched_switch"
  ftrace_events: "power/suspend_resume"
  atrace_categories: "view"
  atrace_categories: "wm"
  atrace_categories: "am"
  atrace_apps: "com.mockdonalds.app"
} } }
duration_ms: 10000
EOF

# Pull trace
adb pull /data/misc/perfetto-traces/trace.perfetto-trace ./traces/
```

### iOS — Instruments

```bash
# Launch with Instruments (Time Profiler)
xcrun xctrace record \
  --device 'iPhone 16' \
  --template 'Time Profiler' \
  --output ./traces/{feature}_profile.trace \
  --launch -- /path/to/iOSApp.app

# Launch with Instruments (App Launch)
xcrun xctrace record \
  --device 'iPhone 16' \
  --template 'App Launch' \
  --output ./traces/{feature}_launch.trace \
  --launch -- /path/to/iOSApp.app

# Instruments templates available:
# - Time Profiler — CPU usage, hot functions
# - App Launch — startup time breakdown
# - Allocations — memory usage, leaks
# - Core Animation — frame rate, GPU
# - Network — HTTP request timing
# - Energy Log — battery impact
```

### Trace Output Locations

| Platform | Tool | Output |
|----------|------|--------|
| Android | Macrobenchmark | `testing/e2e-tests/build/outputs/connected_android_test_additional_output/` |
| Android | Manual Perfetto | `./traces/` (local) |
| iOS | Instruments | `./traces/` (local) |

## Mode: Analyze Traces

### Android — Perfetto

```bash
# Open in Perfetto UI (browser)
open https://ui.perfetto.dev
# Then drag-and-drop the .perfetto-trace file

# Or use trace_processor for CLI analysis
trace_processor --query "
  SELECT name, dur/1e6 as duration_ms
  FROM slice
  WHERE name LIKE '%{feature}%'
  ORDER BY dur DESC
  LIMIT 20
" trace.perfetto-trace
```

**Key things to look for:**
- Main thread blocks > 16ms (jank)
- Custom trace sections (`{feature}_*`) duration
- Network request timing
- Layout/measure passes per frame
- GC pauses

### iOS — Instruments

```bash
# Export Instruments data to CLI-parseable format
xcrun xctrace export --input ./traces/{feature}_profile.trace --output ./traces/{feature}_export/

# Open in Instruments GUI
open ./traces/{feature}_profile.trace
```

**Key things to look for:**
- Main thread hangs > 16ms
- Excessive allocations / ARC overhead
- SwiftUI body re-evaluation frequency
- Network request waterfall
- Energy impact spikes

## Benchmark Conventions

- Benchmark class names end with `Benchmark`
- Benchmarks live in `testing/e2e-tests/` (Android) or E2ETests test plan (iOS)
- Use `StartupMode.COLD` for startup, `StartupMode.WARM` for in-app scenarios
- Minimum 5 iterations for statistical significance
- Add custom trace sections with descriptive names: `{feature}_{operation}`
- Never commit trace files — add `*.perfetto-trace` and `*.trace` to `.gitignore`

## Key Rules

- **Benchmarks require a real device/emulator/simulator** — they don't run on CI host
- **Don't benchmark on debug builds** — use release/benchmark build variants
- **Custom trace sections are low-overhead** — safe for production code
- **Analyze before optimizing** — measure first, don't guess at bottlenecks
- **Compare before/after** — always capture a baseline before changes

## Post-Change Verification (add mode only)

When adding benchmarks, run `verify` to ensure test code compiles. Running the benchmarks themselves requires a device/emulator/simulator.
