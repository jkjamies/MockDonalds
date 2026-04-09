---
name: add-tests
description: Combined unit + UI test gap-filling for all changed code. Analyzes the branch diff and creates missing tests for domain logic, data access, presenters, and UI screens.
---

# Add Tests

Comprehensive test gap-filling that combines unit tests and UI tests.

## Steps

### 1. Analyze Changes

```bash
git diff origin/main...HEAD --name-only -- '*.kt'
```

Categorize changed files:
- `impl/domain/` → needs unit tests (use case logic)
- `impl/data/` → needs unit tests (repository logic)
- `impl/presentation/src/commonMain/` → needs presenter unit tests
- `impl/presentation/src/androidMain/` → needs UI tests (Robot pattern)
- `test/` → fake changes may require updating existing tests
- `api/` → contract changes may require updating tests + fakes

### 2. Fill Unit Test Gaps

Follow the `add-unit-tests` skill instructions:
- Read `.agents/skills/add-unit-tests/SKILL.md` for templates and conventions
- Create missing test files for Impl classes, repositories, and presenters
- Ensure fakes exist in `test/src/commonMain/` for all abstract use cases

### 3. Fill UI Test Gaps

Follow the `add-ui-tests` skill instructions:
- Read `.agents/skills/add-ui-tests/SKILL.md` for templates and conventions
- Create missing UiTest, UiRobot, StateRobot for new/changed screens
- Ensure TestTags exist in `api/navigation/` for testable elements
- Ensure AndroidManifest.xml exists in androidDeviceTest

### 4. Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes. It will:

- Detect which modules have new or changed test files
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch test naming violations, Robot pattern issues, and convention problems

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
