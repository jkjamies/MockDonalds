---
name: code-review
description: Review code changes against the default branch, checking architecture rules, naming conventions, test coverage, and forbidden patterns. Use before merging or to validate work.
---

# Code Review

Diff-based code review that checks all changes against the project's architecture rules. This skill is a thin driver — the authoritative rules live in the standards listed below.

## Reference Standards

Every rule check resolves to one of these documents. Read them before reviewing; cite them in findings.

- **Architecture & layer isolation**: `.agents/standards/architecture.md`
- **Naming conventions**: `.agents/standards/naming-conventions.md`
- **Dependency injection & Circuit/CenterPost contracts**: `.agents/standards/dependency-injection.md`
- **Forbidden patterns** (banned APIs, raw coroutines, mocks, etc.): `.agents/standards/forbidden-patterns.md`
- **Testing overview + quality standards**: `.agents/standards/testing.md`
- **Test level details**: `testing-unit.md`, `testing-ui-component.md`, `testing-navint.md`, `testing-e2e.md`, `testing-architecture.md`
- **Build config & market/env**: `.agents/standards/build-config.md`
- **Design system**: `.agents/standards/design-system.md`
- **Convention plugins**: `.agents/standards/convention-plugins.md`

## Steps

### 1. Get the diff

```bash
git diff origin/main...HEAD
git diff --name-only origin/main...HEAD
```

### 2. Check each changed file against the standards

For every changed file, walk the relevant standards above and flag violations. Do **not** re-state rules inside this skill — cite the standard and section where the violation lives. A finding should look like "layer isolation (`architecture.md` → 'Layer dependency rules')", not a prose re-derivation.

Scope of checks, by file type:
- Kotlin production code → architecture, naming, DI, forbidden-patterns
- Kotlin test code → testing.md quality standards + the matching test-level standard
- Swift production code → architecture (iOS section), forbidden-patterns (iOS section)
- Swift test code → testing-unit.md (iOS) or testing-navint.md (iOS) or testing-e2e.md (iOS)
- `build.gradle.kts` → convention-plugins.md
- `core/build-config/` → build-config.md (Harness boundary, facade rule)
- `AGENTS.md` files → testing-architecture.md (Konsist-enforced per-module requirement)

### 3. Check test coverage for changed production code

Every changed production class should have matching test coverage at the right level. The canonical mapping lives in `.agents/standards/testing.md` → "Test level selection" and each per-level standard. Flag missing tests explicitly, naming the expected file path.

### 4. Additional sanity checks (skill-specific)

These don't have a standards home yet — flag them directly:
- No secrets or credentials committed (`.env`, API keys, tokens, signing material)
- No hardcoded URLs — endpoints come from `core:build-config` per `build-config.md`
- New features have an `AGENTS.md` file (Konsist will fail the build otherwise, but catch it at review time)
- PR scope looks coherent — don't merge drive-by refactors bundled with a feature change

### 5. Output format

```
## [ERROR] {file}:{line} — {rule} ({standards-file} → {section})
{what needs to change}

## [WARNING] {file}:{line} — {potential issue} ({standards-file} → {section})
{explanation}

## [INFO] {observation}
{context}
```

Errors must be fixed before merge. Warnings should be addressed. Info is advisory.
