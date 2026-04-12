---
name: verify-smart
description: Diff-aware verification that scopes checks to changed modules. Faster than full verify for targeted changes. Always runs architecture tests.
---

# Verify Smart

Scoped verification based on what actually changed on the current branch. Faster than `verify` when you've only touched 1–2 modules, but still catches architecture violations.

**The authoritative module-to-Gradle-task mapping, decision logic, and failure interpretation live in `.agents/standards/verification.md` → "Scoped Verification (verify-smart)".** This skill is a thin driver; keep the standard as the single source of truth.

## When to use

- During iterative development when `verify` is too slow.
- After targeted changes to 1–2 features or modules.
- For documentation-only changes (architecture tests catch stale AGENTS.md references).

For broader validation, use `verify`. For pre-merge / pre-PR, use `verify-ci`.

## Steps

1. Detect changed files:
   ```bash
   git diff origin/main...HEAD --name-only    # branch changes
   git diff --name-only                        # uncommitted on main
   ```
2. Look up each changed path in the "Module-to-Gradle-Task Mapping" table in `.agents/standards/verification.md`.
3. Apply the "verify-smart Decision Logic" section of the same standard to choose which steps to run.
4. Always run `:testing:architecture-check:test` (fast, catches structural issues regardless of what changed).

## Escalation

If `verify-smart` passes but the change touches anything in `.agents/standards/verification.md` → "When to escalate to verify-ci locally" (R8/Proguard, `expect`/`actual`, cinterop, `core:build-config` schema), run `verify-ci` before merging.
