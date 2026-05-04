# testing/kiosk/benchmarks

## Purpose
Macrobenchmarks for the kiosk app (cold/warm startup → AttractScreen). Mirrors `:testing:mobile:benchmarks` (consumer) but targets `:kioskApp`'s `benchmark` build variant (R8-minified, profileable for Perfetto).

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| `KioskStartupBenchmark` | `main/.../` | `coldStartup` + `warmStartup` measureRepeated runs. Hot startup is intentionally omitted for the same reason the consumer omits it (R8 + emulator make `StartupTimingMetric` unable to read trace events). |

## Targeting
- `targetProjectPath = ":kioskApp"`.
- `experimentalProperties["android.experimental.self-instrumenting"] = true` — same Google canonical setup as the consumer benchmark module.
- Only the `benchmark` build variant is enabled (`androidComponents.beforeVariants` filter).
- `TARGET_PACKAGE = "com.mockdonalds.kiosk.core"` — derived from kiosk applicationId base + `.core` market suffix; hardcoded because self-instrumenting makes `targetContext.packageName` return the test APK's package.

## Run Command
```bash
./gradlew :kioskApp:installCoreIntBenchmark :testing:kiosk:benchmarks:connectedBenchmarkAndroidTest
```

## Spec
Full spec: [`../../../specs/kiosk-app.md`](../../../specs/kiosk-app.md).
