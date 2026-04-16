---
name: migrate
description: Execute cross-cutting migrations — library swaps, pattern changes, API version upgrades, or architecture refactors. Handles phased rollout, coexistence, and rollback planning.
---

# Migrate

Execute a migration from one pattern, library, or approach to another across the codebase.

**Parameters**: migration description (optional if spec provides it)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — short description, e.g., `/migrate order repo from in-memory to ktor`.
2. **`@file` reference** — e.g., `/migrate @specs/auth-to-oauth2.md`. The CLI resolves the file and includes its content. Extract from/to states, scope, phasing, and rollback plan from the spec. Template: `.agents/templates/migrate-spec.md`.
3. **Inline description** — free text describing the migration.

## Reference Standards

- Architecture & module structure: `.agents/standards/architecture.md`
- DI patterns: `.agents/standards/dependency-injection.md`
- Convention plugins: `.agents/standards/convention-plugins.md`
- CenterPost interactors: `.agents/standards/centerpost.md`

## Steps

### 1. Audit Current State

- Read all files involved in the "from" state
- Map the full dependency graph of the thing being migrated
- Identify all consumers (features, core modules, tests, iOS)
- Document current behavior as a baseline for verification

### 2. Assess Migration Strategy

| Strategy | When to Use | Risk |
|----------|-------------|------|
| **Big-bang** | Small scope, low risk, no phasing needed | Must be correct in one pass |
| **Phased** | Large scope, can ship incrementally | Coexistence complexity |
| **Strangler-fig** | Critical path, zero-downtime required | Bridge/adapter overhead |

Choose based on scope and risk. Default to **phased** for anything touching more than one feature.

### 3. Introduce New Pattern (if phased/strangler)

- Add new abstractions alongside existing ones
- Create bridge/adapter if old and new must coexist
- Wire feature flag to toggle between old and new paths if applicable:
  ```kotlin
  val flag = FeatureFlag(key = "use_new_{thing}", defaultValue = false)
  ```

### 4. Migrate Consumers

Work through consumers one at a time, verifying each:

**For each consumer:**
1. Update imports and type references
2. Adapt to new API surface
3. Update tests to cover new path
4. Update fakes if abstractions changed
5. Run `verify-smart` to confirm the consumer still works

**Order of migration:**
1. Core modules (upstream dependencies first)
2. Feature `impl/data` layers
3. Feature `impl/domain` layers
4. Feature `impl/presentation` layers
5. Feature `test/` fakes
6. iOS layer
7. Test suites (unit, UI, navint, e2e)

### 5. Verify Behavioral Equivalence

After migrating all consumers:
- All existing tests must pass without modification (behavior preserved)
- Any test modifications must be intentional and documented
- Run full `verify` to confirm cross-module consistency

### 6. Remove Old Code

Only after all consumers are migrated and verified:
- Delete old implementation files
- Remove old dependency declarations
- Remove bridge/adapter code
- Remove migration feature flag
- Remove old DTOs, mappers, data sources
- Update AGENTS.md files

### 7. Update Documentation

- Update `AGENTS.md` for affected features and core modules
- Update `.agents/standards/` if the migration changes a documented convention
- Update `CLAUDE.md` if build commands or key versions changed

## Common Migration Types

### Library Swap
```
From: library-a → To: library-b
1. Add library-b dependency
2. Create new impl using library-b
3. Migrate consumers
4. Remove library-a dependency
```

### Pattern Change
```
From: pattern-a → To: pattern-b
1. Document both patterns
2. Create new pattern alongside old
3. Migrate file by file
4. Remove old pattern
```

### API Version Upgrade
```
From: /v1/resource → To: /v2/resource
1. Add v2 DTO alongside v1
2. Add v2 data source
3. Update repository to use v2
4. Remove v1 DTO and data source
```

### In-Memory to Real Implementation
```
From: flowOf(fakeData) → To: HttpClient + real API
1. Create RemoteDataSource interface + impl
2. Create DTO + mapper
3. Update RepositoryImpl to use data source
4. Update tests with fake data source
```

## Dependency Changes

When adding or removing libraries:

| Action | Where | Convention |
|--------|-------|------------|
| Add dependency | `build-logic/src/main/.../` or module `build.gradle.kts` | Use version catalog (`libs.{name}`) |
| Remove dependency | Same | Verify zero imports before removing |
| Version bump | `gradle/libs.versions.toml` | Update version catalog entry |

## Key Rules

- **Never break the build mid-migration** — each step must compile and pass tests
- **Migrate one consumer at a time** — verify after each, not at the end
- **Preserve behavior** — migration changes implementation, not functionality (unless explicitly requested)
- **Feature-flag large migrations** — if it takes more than one session, gate with a flag
- **Update AGENTS.md** — migration changes are exactly the kind of thing AGENTS.md must reflect
- **Check iOS parity** — Kotlin migrations often require SwiftUI-side changes

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the full `verify` skill since migrations are cross-cutting. The skill will run: lint, unit tests, architecture tests (Konsist + Harmonize), and full build.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
