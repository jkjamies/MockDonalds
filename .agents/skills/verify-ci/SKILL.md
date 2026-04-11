---
name: verify-ci
description: Full pre-merge verification pipeline — every lint, every test level (unit, architecture, navint, e2e), and a full `./gradlew assemble` across every KMP target and variant. Slow (~5+ min). Use when the `verify` skill isn't enough — before opening a PR, when touching R8/Proguard, cinterop, or market config schema.
---

# Verify (CI)

Prove the code ships. Every target, every variant, every market combo, both platforms, both build types. This is what CI runs; you rarely need it locally.

Target runtime: thorough, not fast. Plan for ~5–10 min warm, longer cold.

For the fast inner-loop pipeline (single debug build per platform, default `us-dev`), use the `verify` skill instead. See `.agents/standards/verification.md` for the full local-vs-CI rationale.

## When to run this locally

- Before opening a PR, as a last sanity check.
- When the change touches R8/Proguard keep-rules, `expect`/`actual` splits, cinterop `.def` files, or market config schema.
- When `verify` passed but CI failed and you want to reproduce locally.

Otherwise, `verify` is enough. Don't pay this cost on every save.

## Steps

Run in order. Stop and fix failures before proceeding.

### 1. Detekt (Kotlin lint)
```bash
./gradlew detektMetadataCommonMain
```

### 2. SwiftLint (Swift style)
```bash
swiftlint --config .swiftlint.yml
```

### 3. Unit tests (Kotest — Android host)
```bash
./gradlew testAndroidHostTest
```

### 4. iOS unit tests
```bash
xcodebuild test \
  -scheme iOSApp \
  -testPlan UnitTests \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

### 5. Konsist (Kotlin architecture)
```bash
./gradlew :testing:architecture-check:test
```

### 6. Harmonize (iOS architecture)
```bash
swift test --package-path iosApp/ArchitectureCheck
```

### 7. Android navint tests (requires emulator)
```bash
./gradlew :testing:navint-tests:connectedAndroidDeviceTest
```

### 8. iOS navint tests (requires simulator)
```bash
xcodebuild test \
  -scheme iOSApp \
  -testPlan NavIntTests \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

### 9. Android e2e tests (requires device/emulator)
```bash
./gradlew :testing:e2e-tests:connectedAndroidTest
```

### 10. iOS e2e tests (requires simulator)
```bash
xcodebuild test \
  -scheme iOSApp \
  -testPlan E2ETests \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

### 11. Full assemble (every target × every variant)
```bash
./gradlew assemble
```

Fires ~1800 tasks on warm cache. Builds every KMP target (`iosArm64`, `iosSimulatorArm64`, `iosX64`, Android debug + release) for every module, runs R8/minify on Android release, and LLVM-opt on iOS release frameworks. This is the step that catches R8 keep-rule failures, obfuscation-strips-reflected-symbol bugs, cinterop edge cases, and `expect`/`actual` mismatches — the rare-but-real failure modes `verify` skips.

### 12. (Optional) Market matrix

If the change touches `core:build-config` or market-specific logic, build every combo to prove the matrix is clean. Phase 1: us-dev, us-prod, de-dev, de-prod.

```bash
./gradlew :androidApp:assembleDebug -Pmarket=us -Penv=dev
./gradlew :androidApp:assembleDebug -Pmarket=us -Penv=prod
./gradlew :androidApp:assembleDebug -Pmarket=de -Penv=dev
./gradlew :androidApp:assembleDebug -Pmarket=de -Penv=prod
```

Phase 2 will replace this with a `validateAllMarkets` Gradle task that parses every combo file through the schema without compiling — cheap enough to move into the `verify` skill.

## Interpreting failures

See `.agents/standards/verification.md` — "Failure Interpretation" section — for the full list of failure modes and how to diagnose each tool's output.

Quick reference for CI-only failures (the ones `verify` doesn't catch):

- **`assemble` fails but `:androidApp:assembleDebug` passed**: likely R8/Proguard keep-rule regression on `:androidApp:assembleRelease`, or an `iosArm64`/`iosX64` target-specific compile failure. Check the task that failed — Gradle prints the module + target in the error path.
- **iOS release framework linking fails**: LLVM-opt on release frameworks trips on cinterop edge cases or `@ObjCName`/export-list mismatches. Check `composeApp/build/bin/iosArm64/releaseFramework/` for linker output.
- **Market matrix fails for one combo only**: missing override in that `markets/*.properties` file, or a key typo. The Phase 1 smoke test only covers the default combo — matrix failures land here.
