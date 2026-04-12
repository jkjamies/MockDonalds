---
name: lint-branch
description: Fast pre-commit lint check — runs Detekt on commonMain Kotlin and SwiftLint on changed Swift files only. Targets the ~5s inner loop you run 20× a day. For thorough diff-scoped verification (lint + unit + architecture), use verify-smart.
---

# Lint Branch

Prove the code you just touched passes lint, fast. No full-repo scans, no unit tests, no architecture checks. Just style/unused-decl feedback on the files that matter right now.

Target runtime: ~5s warm. Slower than "nothing" but faster than anything that invokes real compilation.

## When to use which lint skill

| Skill | Scope | Runs | Use when |
|---|---|---|---|
| `lint-branch` (this) | changed files only | Detekt (commonMain) + SwiftLint (changed .swift) | Pre-commit sanity, 20× a day |
| `verify-smart` | changed modules | lint + unit + arch per affected module | Before pushing, once per iteration |
| `verify` | whole repo | full lint + unit + arch + debug build both platforms | End of a task, before opening PR |

If you don't know which to pick: `lint-branch` first. If it passes and you also want unit/arch confidence, escalate to `verify-smart`.

## Coverage limitations

This skill deliberately does NOT cover:
- **`androidMain` / `iosMain` / `iosSimulatorArm64Main` Kotlin** — platform-specific source sets. If you touched `actual` impls, expect/actual splits, or anything under a platform source dir, run `verify-smart` instead or invoke the per-source-set detekt task (`./gradlew :module:detektAndroidMain`).
- **Swift architecture tests** — Harmonize lives in `verify-smart` / `run-arch-tests`.
- **Kotlin architecture tests** — Konsist lives in `verify-smart` / `run-arch-tests`.
- **Compilation errors** — Detekt doesn't compile Kotlin. A syntactically broken file may slip through; `verify-smart` catches it at the unit-test compile step.

The tradeoff is deliberate: fast feedback for the 90% case (commonMain + Swift edits) at the cost of missing platform-specific Kotlin. The other skills own the 10% case.

## Steps

### 1. Find changed files

```bash
BASE="${BASE:-origin/main}"
git fetch origin main --quiet 2>/dev/null || true
CHANGED=$(git diff --name-only "${BASE}...HEAD" -- '*.kt' '*.kts' '*.swift')
```

If `CHANGED` is empty, print "lint-branch: no Kotlin or Swift changes vs. $BASE" and exit 0.

### 2. Detekt (commonMain)

If any `.kt` or `.kts` file changed, run:

```bash
./gradlew detektMetadataCommonMain
```

This is module-unaware — Gradle picks the right set of tasks based on the configured source sets. It covers every `commonMain` Kotlin file in every module; changes under `androidMain`/`iosMain` are out of scope (see limitations above).

Detekt auto-corrects formatting issues (`autoCorrect = true` in the convention plugin). Re-stage any auto-corrected files before committing.

### 3. SwiftLint (changed files only)

If any `.swift` file changed, feed them to SwiftLint directly — `.swift` files are file-scoped, no module resolution needed:

```bash
echo "$CHANGED" | grep '\.swift$' | xargs -r swiftlint lint --config .swiftlint.yml
```

For auto-correctable violations, re-run with `swiftlint --fix` on the same file list.

### 4. Report

Summarize:
- Files checked (counts: `X .kt/.kts`, `Y .swift`)
- Violations found (0 or N)
- Any auto-corrections applied (tell the user to re-stage)

If violations remain that can't auto-correct, list them with file:line and stop — do NOT proceed to commit.

## Interpreting failures

- **Detekt violation**: check the rule name and file:line. Most common: `UnusedImports`, `UnusedPrivateMember`, `MaxLineLength`, `WildcardImport`. See `.agents/standards/code-style.md` for the rule set and `.agents/standards/forbidden-patterns.md` for anything Detekt flags that's also architecturally banned.
- **SwiftLint violation**: same shape — rule + file:line. `.swiftlint.yml` at the repo root is the config.
- **"No Kotlin or Swift changes"**: either you haven't modified anything vs. `origin/main`, or `BASE` points at the wrong branch. Pass `BASE=origin/your-branch` if you need a different comparison point.

## Related

- `.agents/standards/verification.md` — full pipeline context, failure interpretation
- `.agents/standards/code-style.md` — Detekt/ktlint/SwiftLint rules
- `.agents/skills/verify-smart/` — diff-scoped full verification (next step up)
- `.agents/skills/find-dead-code/` — broader unused-declaration sweep across the whole repo
