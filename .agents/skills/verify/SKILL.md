---
name: verify
description: Fast local verification pipeline — lint, unit tests, architecture checks, SwiftLint, and one debug build per platform. Use after any code change to prove correctness on both platforms without paying CI-scale build costs.
---

# Verify

Prove the code is correct and compiles on both platforms. Fast, local, no market matrix, no release variants.

Target runtime: under ~60s warm, under ~2 min cold.

See `.agents/standards/verification.md` for the full local-vs-CI rationale and what the slower CI pipeline covers on top of this. For the full pre-merge pipeline (every target, every variant, every market combo), use the `verify-ci` skill.

## Parameters

- `market` (optional, default `us`) — which market to build (`us`, `de`, ...).
- `env` (optional, default `dev`) — which env to build (`dev`, `prod`, ...).

If the user says something like "verify with de/prod" or "verify market=de", use those values for steps 6 and 7. Otherwise build `us-dev`. This lets someone who's specifically iterating on a market change prove that one combo without running the whole matrix — and everyone else stays on the fast default.

The iOS configuration name is `{MARKET-uppercase}-{Env-titlecase}` — `us` + `dev` → `US-Dev`, `de` + `prod` → `DE-Prod`.

## Steps

Run in order. Stop and fix failures before proceeding.

### 1. Detekt (Kotlin lint)
```bash
./gradlew detektMetadataCommonMain
```

### 2. Unit Tests (Kotest)
```bash
./gradlew testAndroidHostTest
```

### 3. Konsist (Kotlin architecture)
```bash
./gradlew :testing:architecture-check:test
```

### 4. Harmonize (iOS architecture)
```bash
swift test --package-path iosApp/ArchitectureCheck
```

### 5. SwiftLint (iOS style)
```bash
swiftlint --config .swiftlint.yml
```

### 6. Android debug build
```bash
./gradlew :androidApp:assembleDebug -Pmarket=$MARKET -Penv=$ENV
```

Builds one combo, one build type. Default `us-dev`. Proves the shared Kotlin compiles for Android and the app links. No market matrix — this is only the combo the user asked about (or the default).

### 7. iOS debug build (simulator-arm64 only)
```bash
xcodebuild build \
  -scheme iOSApp \
  -configuration ${MARKET_UPPER}-${ENV_TITLE} \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -sdk iphonesimulator \
  | tail -20
```

Simulator-arm64 only. Skips `iosArm64` (device) and `iosX64` (legacy Intel sim) — those are CI's job. Configuration name is derived from the parameters: `us` + `dev` → `US-Dev`, `de` + `prod` → `DE-Prod`, etc.

## What this skill deliberately does NOT run

- `./gradlew assemble` — 5+ minutes, builds every target × every variant on every module.
- Market matrix — building every combo separately.
- Release builds — R8/minify on Android, LLVM-opt on iOS.
- `iosArm64` (device target) and `iosX64` (Intel sim).
- Navint tests, E2E tests — they need a running device/sim/emulator and are slow. Run them when the change warrants it (navigation, Circuit wiring, journey flows).

All of the above belong in CI. Pay that cost once per PR, not once per save. If you need the full pipeline locally, invoke `verify-ci` instead.

## When to escalate to verify-ci locally

Only when the change specifically touches:
- R8/Proguard keep-rules → `./gradlew :androidApp:assembleRelease`
- `expect`/`actual` source-set splits → build both platforms explicitly
- cinterop definitions or `.def` files → build `iosArm64` too
- Market config schema or combo files → build a second combo (`-Pmarket=de -Penv=prod`), not the whole matrix

For anything else, the 7 steps above are enough.

## Interpreting Failures

See `.agents/standards/verification.md` — "Failure Interpretation" section — for the full list of failure modes and how to diagnose each tool's output.
