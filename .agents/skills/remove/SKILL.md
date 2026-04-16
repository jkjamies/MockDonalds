---
name: remove
description: Clean removal of features, screens, use cases, repositories, or other code across all layers. Handles dependency analysis, file deletion, reference cleanup, and test updates.
---

# Remove

Tear down existing code cleanly across all affected layers with zero dead code left behind.

**Parameters**: target description (optional if spec provides it)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — just the target, e.g., `/remove order CartSummary screen` or `/remove deals feature`.
2. **`@file` reference** — e.g., `/remove @specs/remove-legacy-rewards.md`. The CLI resolves the file and includes its content. Extract target, dependencies, what to preserve, and replacement info from the spec. Template: `.agents/templates/remove-spec.md`.
3. **Inline description** — free text describing what to remove and any constraints.

## Reference Standards

- Architecture & module structure: `.agents/standards/architecture.md`
- Naming conventions: `.agents/standards/naming-conventions.md`
- DI patterns: `.agents/standards/dependency-injection.md`

## Steps

### 1. Dependency Analysis — CRITICAL

Before deleting anything, map every reference to the target:

```
Search for:
- Direct imports of target types
- Navigation references (navigator.goTo, screen registrations)
- DI bindings (@ContributesBinding for target interfaces)
- Build file dependencies (project(":features:{name}:..."))
- Test references (fakes, test scenarios, robots)
- iOS references (SwiftUI views, navigation, view models)
- AGENTS.md cross-feature dependency mentions
- Deep link routes
- Analytics events tied to the target
- Feature flags tied to the target
```

### 2. Identify What Must Survive

Before removing, explicitly confirm:
- Types used by other features (shared via api/ modules)
- Config fields shared with other services
- Analytics event history (platform-side, not code)

### 3. Update Dependents First

For each dependent found in step 1:
- **Navigation references** — remove `navigator.goTo({Removed}Screen)` or redirect to replacement
- **Import references** — remove unused imports, update types if replacement exists
- **Build dependencies** — remove `project(":features:{name}:...")` lines
- **Test references** — remove test scenarios that exercise the removed code
- **iOS references** — remove SwiftUI views, navigation registrations, view models

### 4. Delete Target Files

Remove in reverse dependency order (consumers first, then providers):

| Order | Layer | Files |
|-------|-------|-------|
| 1 | Tests | `*Test.kt`, `*UiRobot.kt`, `*StateRobot.kt` |
| 2 | test/ fakes | `Fake*.kt` |
| 3 | impl/presentation | Presenter, UiState, Events, UI |
| 4 | impl/data | RepositoryImpl, DataSources, DTOs, mappers |
| 5 | impl/domain | UseCase impls, Repository interfaces |
| 6 | api/navigation | Screen objects, TestTags |
| 7 | api/domain | Models, abstract use cases |
| 8 | iOS | SwiftUI views, view models, navigation |
| 9 | Build files | build.gradle.kts for deleted modules |
| 10 | Directories | Empty module directories |

### 5. Clean Up Build Files

- Remove dependency declarations in surviving modules' `build.gradle.kts`
- Remove deleted modules from any explicit `settings.gradle.kts` entries (auto-discovery should handle most cases)

### 6. Clean Up Feature Flags and Config

- Remove `FeatureFlag` definitions for the deleted feature
- Remove build-config fields only used by the deleted feature (verify no other consumers first)
- Remove analytics event classes for the deleted feature

### 7. Update Documentation

- Delete `features/{name}/AGENTS.md` if removing entire feature
- Update other features' `AGENTS.md` — remove cross-feature dependency references
- Update root documentation if the feature was mentioned

### 8. Verify No Dead Code Remains

Run `/find-dead-code` after removal to catch:
- Orphaned TestTags with no screen
- Orphaned Screen objects with no navigation
- Orphaned Fakes with no test references
- Unused imports

## Target Types

| Target | Scope | Typical Files Affected |
|--------|-------|----------------------|
| Feature | Entire `features/{name}/` | All 6 submodules + tests + iOS + AGENTS.md |
| Screen | One screen within a feature | api/navigation + impl/presentation + tests |
| Use case | One interactor | api/domain + impl/domain + test/ fake + tests |
| Repository | One repository | impl/domain interface + impl/data impl + data sources + DTOs + tests |
| Core module | Entire `core/{name}/` | Core module + all feature consumers |

## Key Rules

- **Never delete without dependency analysis** — broken builds are worse than dead code
- **Delete completely** — partial removal is the #1 source of dead code
- **Update AGENTS.md** — stale documentation is misleading at scale
- **Check iOS** — every Kotlin removal may have a SwiftUI counterpart
- **Verify build** — full build must pass after removal

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run `verify full` since removal can affect any module. The skill will run: lint, unit tests, architecture tests (Konsist + Harmonize), and full build.

Additionally, run `/find-dead-code` to confirm no orphaned code remains.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
