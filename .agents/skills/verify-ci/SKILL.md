---
name: verify-ci
description: Full pre-merge verification pipeline — every lint, every test level (unit, architecture, navint, e2e), and a full `./gradlew assemble` across every KMP target and variant. Slow (~5+ min). Use when the `verify` skill isn't enough — before opening a PR, when touching R8/Proguard, cinterop, or market config schema.
---

# Verify (CI)

Prove the code ships. Every target, every variant, every market combo, both platforms, both build types. This is what CI runs; you rarely need it locally.

Target runtime: thorough, not fast. Plan for ~5–10 min warm, longer cold.

**The authoritative command list, pipeline diagram, and failure interpretation live in `.agents/standards/verification.md` → "Full Pipeline (CI)" and "Failure Interpretation".** This skill is a thin driver; keep the standard as the single source of truth.

For the fast inner-loop pipeline (single debug build per platform, default `us-dev`), use the `verify` skill instead.

## When to run this locally

- Before opening a PR, as a last sanity check.
- When the change touches R8/Proguard keep-rules, `expect`/`actual` splits, cinterop `.def` files, or `core:build-config` schema.
- When `verify` passed but CI failed and you want to reproduce locally.
- See `.agents/standards/verification.md` → "When to escalate to verify-ci locally" for the exact trigger list.

Otherwise, `verify` is enough. Don't pay this cost on every save.

## Steps

Run the full pipeline from `.agents/standards/verification.md` → "Full Pipeline (CI)" section, in order. Stop and fix failures before proceeding.

Summary (see the standard for exact commands — pre-flight market-config check, then fully symmetric across both platforms by concern: lint → unit → architecture → UI component → nav/int → e2e → build):

0. **Pre-flight: `./gradlew :core:build-config:validateAllMarkets`** — owned by the `validate-all-markets` skill. Aggregates every combo-file violation in one pass; gates the entire pipeline.
1. Detekt — Kotlin lint
2. SwiftLint — Swift style
3. Kotest — Kotlin pure-logic unit tests (Android host)
4. iOS unit tests — Swift Testing pure-logic, `UnitTests` plan, `iosAppTests/Unit/` (requires simulator)
5. Konsist — Kotlin architecture
6. Harmonize — iOS architecture
7. Android UI component tests — `connectedAndroidDeviceTest` (requires emulator)
8. iOS UI component tests — ViewInspector Robot, `UIComponentTests` plan, `iosAppTests/UIComponent/` (requires simulator)
9. Android navint tests (requires emulator)
10. iOS navint tests (requires simulator)
11. Android e2e tests (requires device/emulator)
12. iOS e2e tests (requires simulator)
13. `./gradlew assemble` — every target × every variant

## Market matrix

The pre-flight `validate-all-markets` step (above) covers schema/format drift across every combo without compiling. If the change also touches Kotlin code that consumes config (e.g. `AppBuildConfigImpl`, anything reading `BuildConfig.*`), additionally build each Phase 1 combo (`us-dev`, `us-prod`, `de-dev`, `de-prod`) to prove the merge → BuildKonfig → compile chain still resolves end-to-end.
