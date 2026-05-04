# Macrobenchmarks (consumer)

## Purpose

Android instrumented macrobenchmark module that measures startup, frame timing, and other runtime performance against the **consumer app's** release-representative (R8-minified) target. Produces Perfetto traces for regression detection.

> Kiosk has its own peer suite at `:testing:kiosk:benchmarks` targeting `:kioskApp`. See [`../kiosk/benchmarks/AGENTS.md`](../kiosk/benchmarks/AGENTS.md) and [`../../specs/kiosk-app.md`](../../specs/kiosk-app.md).

## How It Works

- Uses `com.android.test` plugin with `targetProjectPath = ":androidApp"`
- Targets the **`benchmark`** build type of `:androidApp` — `initWith(release)` (R8-minified) with debug signing; Perfetto access comes from `<profileable android:shell="true" />` in `androidApp/src/benchmark/AndroidManifest.xml` (NOT `isDebuggable = true` — debuggable disables JIT and invalidates benchmark measurements)
- `experimentalProperties["android.experimental.self-instrumenting"] = true` — benchmark runs in its own process, not inside the target app. This is Google's canonical setup (see [macrobenchmark overview](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)) and is what satisfies AGP's `checkTestedAppObfuscation` when the target is minified but the test APK isn't
- Because self-instrumenting decouples the test APK from the target, `targetContext.packageName` returns the **test** package. Benchmarks must hardcode the target package (see `StartupBenchmark.TARGET_PACKAGE`)
- `AndroidManifest.xml` declares `QUERY_ALL_PACKAGES` so `ProcessTracker` can detect the target process after `am start` — without it, API 30+ package visibility rules return an empty process list and macrobenchmarks fail with "Unable to confirm activity launch completion []"
- `androidComponents.beforeVariants { enable = false }` disables every non-benchmark variant so only benchmark APKs are produced
- Intentionally minimal deps: `benchmark-macro-junit4` + `uiautomator` + `test.runner` + `test.ext.junit`. Do **not** add `compose.ui.test.junit4` — it injects `AppComponentFactoryRegistry` into the target process, which calls Kotlin runtime methods R8 has stripped, crashing the target on launch
- Requires `androidx.benchmark:benchmark-macro-junit4:1.4.x` or newer — 1.3.x has a bug where `amStartAndWait` fails to confirm launch completion under self-instrumenting + profileable targets

## What Gets Tested

| Category | Location | Tests |
|----------|----------|-------|
| Startup | `StartupBenchmark.kt` | Cold / warm startup timing (3 iterations each) |

Hot startup is intentionally omitted: on emulator + R8 targets it completes too fast for `StartupTimingMetric` to observe a trace event, causing "Unable to read any metrics" failures. Hot startup for a well-behaved Compose app is effectively instant and measuring it adds noise without signal.

## Key Types

| Type | Purpose |
|------|---------|
| `StartupBenchmark` | Measures cold and warm startup via `MacrobenchmarkRule` + `StartupTimingMetric` |

## Dependencies

```
benchmarks depends on:
  :androidApp                    — target application (benchmark variant, minified)

benchmarks does NOT depend on:
  features/*                     — uses UI Automator, no direct code access
  compose.ui.test.junit4         — architecturally incompatible with minified target
```

## Running

Self-instrumenting decouples the test APK from the target — AGP does not auto-install the target when the benchmark runs, so **install it explicitly first**:

```bash
# All macrobenchmarks (requires physical device — emulator is blocked by androidx.benchmark)
./gradlew :androidApp:installCoreIntBenchmark :testing:benchmarks:connectedBenchmarkAndroidTest

# Market/env-specific variant
./gradlew :androidApp:installDeProdBenchmark :testing:benchmarks:connectedDeProdBenchmarkAndroidTest
```

**Physical device required.** androidx.benchmark refuses to run on emulators because timings are unrepresentative. Suppress only for local smoke-testing:

```bash
./gradlew :androidApp:installCoreIntBenchmark :testing:benchmarks:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
```

Never suppress EMULATOR in CI — results will be noise.

## Adding Tests

1. Benchmark files must end with `Benchmark` and live under `src/main/kotlin/com/mockdonalds/app/benchmarks/`
2. Use `MacrobenchmarkRule` + one of the stock metrics (`StartupTimingMetric`, `FrameTimingMetric`, `TraceSectionMetric`)
3. Drive the app with `UiDevice` / `UiAutomator` — never reference target app classes by name (obfuscation will rename them)
4. All tests use JUnit4 `@RunWith(AndroidJUnit4::class)`
5. Keep iterations low (~3) to keep CI cost bounded; macrobenchmark already averages across runs
6. Do NOT add journey-style functional assertions here — benchmarks measure timing, not correctness
