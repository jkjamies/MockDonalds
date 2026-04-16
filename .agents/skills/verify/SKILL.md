---
name: verify
description: Verification pipeline with three scopes — diff (default, changed modules only), full (whole project lint + unit + arch + builds), and all (every test level + every variant + full assemble). Use after any code change.
---

# Verify

Prove the code you just wrote is correct. Three scopes, one skill.

**Parameters**: scope (optional, default `diff`), market (optional, default `us`), env (optional, default `dev`)

**Usage examples**:
```
/verify                    # diff — scoped to changed modules (~5-15s)
/verify diff               # explicit: same as default
/verify full               # whole project lint + unit + arch + both platform builds (~1-2 min)
/verify all                # every test level + every variant + full assemble (~5-10 min)
/verify full de prod       # full with DE-Prod market/env combo
```

**The authoritative command lists, rationale, and failure interpretation live in `.agents/standards/verification.md`.** This skill is a thin driver; keep the standard as the single source of truth.

## Scope: `diff` (default)

Scoped verification based on what actually changed on the current branch. Fastest option — use during iterative development.

Target runtime: ~5–15s.

### Steps

1. Detect changed files:
   ```bash
   git diff origin/main...HEAD --name-only    # branch changes
   git diff --name-only                        # uncommitted on main
   ```
2. Look up each changed path in the "Module-to-Gradle-Task Mapping" table in `.agents/standards/verification.md`.
3. Apply the "Diff Decision Logic" section of the same standard to choose which steps to run.
4. Always run `:testing:architecture-check:test` (fast, catches structural issues regardless of what changed).

### When to use

- During iterative development — the 20x-a-day inner loop
- After targeted changes to 1–2 features or modules
- For documentation-only changes (architecture tests catch stale AGENTS.md references)

---

## Scope: `full`

Whole project — lint, unit tests, architecture checks, and one debug build per platform. No device/simulator-only tests.

Target runtime: under ~60s warm, under ~2 min cold.

### Parameters

- `market` (optional, default `us`) — which market to build (`us`, `de`, ...).
- `env` (optional, default `dev`) — which env to build (`dev`, `prod`, ...).

iOS configuration name is `{MARKET-uppercase}-{Env-titlecase}` — `us` + `dev` → `US-Dev`, `de` + `prod` → `DE-Prod`.

### Steps

Run the steps listed in `.agents/standards/verification.md` → "Local (the `verify` skill)" section, in order. Stop and fix failures before proceeding.

Summary (see the standard for exact commands):

0. **Pre-flight: `./gradlew :core:build-config:validateAllMarkets`** — runs first because malformed combo files invalidate every downstream step.
1. Detekt — Kotlin lint
2. SwiftLint — Swift style
3. Kotest — Kotlin pure-logic unit tests (Android host)
4. iOS unit tests — Swift Testing pure-logic, `UnitTests` plan, `iosAppTests/Unit/` (requires simulator)
5. Konsist — Kotlin architecture
6. Harmonize — iOS architecture
7. Android debug build for `$MARKET-$ENV`
8. iOS debug build for `$MARKET-$ENV` on simulator-arm64

### When to use

- After any code change as the standard verification
- When `diff` passes but you want to confirm both platforms build
- When changes are broad (touching many modules)

---

## Scope: `all`

Everything. Every target, every variant, every test level, both platforms. This is the most thorough check available.

Target runtime: ~5–10 min warm, longer cold.

### Steps

Run the full pipeline from `.agents/standards/verification.md` → "Full Pipeline (CI)" section, in order. Stop and fix failures before proceeding.

Summary (see the standard for exact commands):

0. **Pre-flight: `./gradlew :core:build-config:validateAllMarkets`** — gates the entire pipeline.
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

### Market matrix

The pre-flight `validate-all-markets` step covers schema/format drift across every combo without compiling. If the change also touches Kotlin code that consumes config (e.g. `AppBuildConfigImpl`, anything reading `BuildConfig.*`), additionally build each Phase 1 combo (`us-dev`, `us-prod`, `de-dev`, `de-prod`) to prove the merge → BuildKonfig → compile chain still resolves end-to-end.

### When to use

- Before opening a PR, as a last sanity check
- When the change touches R8/Proguard keep-rules, `expect`/`actual` splits, cinterop `.def` files, or `core:build-config` schema
- When `full` passed but CI failed and you want to reproduce locally
- See `.agents/standards/verification.md` → "When to escalate" for the exact trigger list

---

## Escalation Path

```
diff → full → all
```

Start with `diff`. If it passes but you're unsure, escalate to `full`. Escalate to `all` only when the change warrants it or before opening a PR. Don't pay for `all` on every save.
