---
name: code-review
description: Review code changes against the default branch, checking architecture rules, naming conventions, test coverage, and forbidden patterns. Use before merging or to validate work.
---

# Code Review

Diff-based code review that checks all changes against the project's architecture rules.

## Reference Standards

Before reviewing, familiarize yourself with these standards:
- Architecture: `.agents/standards/architecture.md`
- Naming: `.agents/standards/naming-conventions.md`
- DI: `.agents/standards/dependency-injection.md`
- Forbidden patterns: `.agents/standards/forbidden-patterns.md`
- Testing: `.agents/standards/testing.md`
- Design system: `.agents/standards/design-system.md`
- Convention plugins: `.agents/standards/convention-plugins.md`

## Steps

### 1. Get the Diff

```bash
git diff origin/main...HEAD
git diff --name-only origin/main...HEAD
```

### 2. Check Architecture Rules

For each changed file, verify against `.agents/standards/architecture.md`:
- Layer isolation (presentation ↛ data/domain impl, domain ↛ presentation, data ↛ presentation)
- Cross-feature imports only reference other features' `api/` modules
- Core modules do not import from features

And against `.agents/standards/naming-conventions.md`:
- All types follow naming patterns (Screen, Presenter, UiState, Event, Impl, Fake, TestTags)
- Correct annotations present (`@Parcelize`, `@CircuitInject`, `@ContributesBinding`)

And against `.agents/standards/forbidden-patterns.md`:
- No banned patterns (wildcard imports, println, !!, raw coroutines, mockk, etc.)

And against `.agents/standards/dependency-injection.md`:
- Presenters use CenterPost interactors only — no direct repository access
- All Impl classes have `@ContributesBinding(AppScope::class)`

### 3. Check Test Coverage

For each new or modified production class:

| Class Pattern | Expected Test Location |
|---------------|----------------------|
| `*Impl.kt` in impl/domain | `*ImplTest.kt` in impl/domain/src/commonTest |
| `*RepositoryImpl.kt` in impl/data | `*RepositoryImplTest.kt` in impl/data/src/commonTest |
| `*Presenter.kt` in impl/presentation | `*PresenterTest.kt` in impl/presentation/src/commonTest |
| `*Ui.kt` in impl/presentation/androidMain | `*UiTest.kt` + `*UiRobot.kt` + `*StateRobot.kt` in androidDeviceTest |
| New abstract use case in api/domain | `Fake*` in test/src/commonMain |

### 4. Check Test Quality

For each new or modified test file, verify against `.agents/standards/testing.md#test-quality-standards`:
- Not a change detector — tests verify behavior, not hardcoded values
- No magic numbers — named constants or derived values
- Presenter tests cover event handling, not just initial state
- UI tests verify interactions, not just rendering
- Fakes only, no mocks

### 5. Additional Checks

- No secrets or credentials committed (`.env`, API keys, tokens)
- No hardcoded URLs (should come from network module configuration)
- Build files use correct convention plugin for their module type
- New features have `AGENTS.md` in their feature directory

### 6. Output Format

Report findings as:

```
## [ERROR] {file}:{line} — {rule violated}
{explanation of what needs to change}

## [WARNING] {file}:{line} — {potential issue}
{explanation}

## [INFO] {observation}
{context}
```

Errors must be fixed before merge. Warnings should be addressed. Info is advisory.
