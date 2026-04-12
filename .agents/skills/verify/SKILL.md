---
name: verify
description: Fast local verification pipeline — lint, unit tests, architecture checks, SwiftLint, and one debug build per platform. Use after any code change to prove correctness on both platforms without paying CI-scale build costs.
---

# Verify

Prove the code you just wrote is correct and compiles on both platforms, fast. No market matrix, no release variants, no device/simulator-only tests.

Target runtime: under ~60s warm, under ~2 min cold.

**The authoritative command list, rationale, and failure interpretation live in `.agents/standards/verification.md`** — specifically the "Local (the `verify` skill)" section and "Failure Interpretation". This skill is a thin driver; keep the standard as the single source of truth.

For the full pre-merge pipeline (every target, every variant, every market combo), use the `verify-ci` skill. For diff-scoped runs during iterative work, use `verify-smart`.

## Parameters

- `market` (optional, default `us`) — which market to build (`us`, `de`, ...).
- `env` (optional, default `dev`) — which env to build (`dev`, `prod`, ...).

If the user says "verify with de/prod" or "verify market=de", use those values for the per-platform debug builds. Otherwise build `us-dev`. Lets someone iterating on a market change prove that one combo without running the whole matrix.

iOS configuration name is `{MARKET-uppercase}-{Env-titlecase}` — `us` + `dev` → `US-Dev`, `de` + `prod` → `DE-Prod`.

## Steps

Run the steps listed in `.agents/standards/verification.md` → "Local (the `verify` skill)" section, in order. Stop and fix failures before proceeding.

Summary (see the standard for exact commands — pre-flight market-config check, then symmetric lint → unit → architecture → build across both platforms):

0. **Pre-flight: `./gradlew :core:build-config:validateAllMarkets`** — owned by the `validate-all-markets` skill. Runs first because malformed combo files invalidate every downstream step.

1. Detekt — Kotlin lint
2. SwiftLint — Swift style
3. Kotest — Kotlin pure-logic unit tests (Android host)
4. iOS unit tests — Swift Testing pure-logic, `UnitTests` plan, `iosAppTests/Unit/` (requires simulator)
5. Konsist — Kotlin architecture
6. Harmonize — iOS architecture
7. Android debug build for `$MARKET-$ENV`
8. iOS debug build for `$MARKET-$ENV` on simulator-arm64

UI component tests (Android `connectedAndroidDeviceTest`, iOS `UIComponentTests` plan) are **not** in local verify — they need a device/simulator and belong to `verify-ci`.

## When to escalate

See `.agents/standards/verification.md` → "When to escalate to verify-ci locally" for the short list of changes that warrant the full pipeline (R8/Proguard, `expect`/`actual` splits, cinterop, market config schema).
