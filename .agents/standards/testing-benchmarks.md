# Macrobenchmark Testing Standards

Macrobenchmarks measure production-representative runtime performance (startup, frame timing, trace-based workloads) against an **R8-minified target** and emit Perfetto traces for regression detection. No test doubles — the point is to measure the real shipping app.

> Shared conventions (test stack, quality standards, fakes, infrastructure) are in [testing.md](testing.md).

## Scope

| What's measured | What's real | What's faked |
|-----------------|-------------|--------------|
| Startup (cold/warm/hot), frame timing, custom trace sections | Everything — minified target app, real DI, real network | Nothing |

## Run Commands

```bash
# All Android macrobenchmarks — physical device required, emulator blocked
./gradlew :testing:benchmarks:connectedBenchmarkAndroidTest

# Market/env-specific variant
./gradlew :testing:benchmarks:connectedCoreIntBenchmarkAndroidTest

# Local smoke-test on emulator (noisy, never CI)
./gradlew :testing:benchmarks:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
```

`androidx.benchmark` refuses to run on emulators by default because timings are unrepresentative. Never suppress `EMULATOR` in CI — results will be noise.

## Key Characteristics

- **Plugin**: `com.android.test` — separate test APK instruments against `:androidApp`
- **Target variant**: `benchmark` build type (`initWith(release)` + `isDebuggable = true`) so Perfetto can collect traces while measuring R8-minified code
- **Self-instrumenting**: `experimentalProperties["android.experimental.self-instrumenting"] = true` — benchmark runs in its own process, not inside the target. Google's canonical macrobenchmark setup; also what satisfies AGP's `checkTestedAppObfuscation` when the target APK is minified and the test APK isn't
- **Variant filter**: `androidComponents.beforeVariants { enable = false }` for every non-benchmark variant — only benchmark APKs are produced
- **Runner**: JUnit4 `@RunWith(AndroidJUnit4::class)` — instrumented tests
- **Element access**: UI Automator (`By.desc(testTag)`) — no direct access to target-app classes (R8 renames them anyway)
- **Location**: `testing/benchmarks/src/main/kotlin/com/jkjamies/sampleplatter/benchmarks/`

## Dependencies

Intentionally minimal: macrobenchmark + UiAutomator + test.runner + test.ext.junit. Do **not** add `compose.ui.test.junit4` — it injects `AppComponentFactoryRegistry` into the target process, which calls Kotlin runtime methods R8 has stripped, crashing the target on launch.

## Test Organization

| Category | Location | Naming |
|----------|----------|--------|
| Startup | `StartupBenchmark.kt` | Files end with `Benchmark` |
| Feature benchmarks | `{Feature}Benchmark.kt` | Files end with `Benchmark` |

## Benchmark Pattern

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val targetPackage = InstrumentationRegistry.getInstrumentation().targetContext.packageName

    @Test
    fun coldStartup() {
        benchmarkRule.measureRepeated(
            packageName = targetPackage,
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

For scroll/frame-timing benchmarks, use `FrameTimingMetric()` and drive scrolling with `UiDevice`/`UiAutomator`.

## Module Dependencies

```
benchmarks depends on:
  :androidApp                    — target application (benchmark variant, minified)

benchmarks does NOT depend on:
  features/*                     — uses UI Automator, no direct code access
  compose.ui.test.junit4         — architecturally incompatible with minified target
  features/*/test                — no fakes, measures the real minified app
```

## When to Add Macrobenchmarks

Add or update macrobenchmarks when:
- Startup performance needs regression tracking for a new market/env combo
- A feature introduces scroll/animation paths where frame-timing regressions matter
- A known-hot code path needs a custom trace section guarded by a benchmark
- R8/ProGuard rules change (benchmarks catch startup regressions from over-aggressive shrinking)

## Enforcement

### Konsist

Boundary rule in `TestBoundaryTest`:
- Benchmark files in `testing/benchmarks/` must end with `Benchmark`

### Harmonize

iOS benchmark files in `iosApp/iosAppBenchmarks/` must end with `PerformanceTest` or `Benchmark`. Harmonize enforces this suffix rule and that performance tests extend `XCTestCase`.

## iOS Layout

iOS benchmarks live in a **dedicated Xcode target** `iosAppBenchmarks` (mirroring Gradle's `:testing:benchmarks` module split). Files live in `iosApp/iosAppBenchmarks/` and run under the `Benchmarks` test plan — separate from the journey-only `E2ETests` plan so performance runs stay isolated from functional correctness. Tests extend `XCTestCase` and use `measure(metrics:)` with `XCTApplicationLaunchMetric`, `XCTOSSignpostMetric`, or other `XCTMetric` types.

```bash
xcodebuild test -scheme iOSApp -testPlan Benchmarks -destination 'platform=iOS Simulator,name=iPhone 17 Pro'
```

| | Android | iOS |
|---|---|---|
| Module / target | `:testing:benchmarks` (Gradle) | `iosAppBenchmarks` (Xcode) |
| Location | `testing/benchmarks/` | `iosApp/iosAppBenchmarks/` |
| Target variant | `benchmark` (R8-minified) | Release build in XCUITest |
| Framework | Macrobenchmark + Perfetto | XCTMetric + XCTApplicationLaunchMetric |
| Test plan | Gradle task | `Benchmarks.xctestplan` |
| Required hardware | Physical device | Simulator or device |
