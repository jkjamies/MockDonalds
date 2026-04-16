# Migrate — Spec Template

> Copy this template, fill in the sections relevant to your migration, and delete sections
> that don't apply. Feed it to the `migrate` skill via `@file` or paste inline.
>
> Usage: `/migrate @specs/auth-to-oauth2.md`

---

## Overview

<!-- One-paragraph summary: what's being migrated, from what to what, and why. -->

**Feature / module**: <!-- what's affected, e.g., core:auth, features:order -->
**Migration type**: <!-- library swap | pattern change | API version upgrade | architecture refactor | platform alignment -->
**Risk level**: <!-- low (internal-only) | medium (changes contracts) | high (cross-feature, data loss possible) -->

---

## From (Current State)

<!-- Describe what exists today in enough detail that someone unfamiliar could understand
     the starting point. Reference specific files, patterns, and dependencies. -->

**Pattern / library / approach**:

**Key files**:
- `path/to/current/implementation.kt`
- ...

**How it works today**:
<!-- e.g., Auth currently uses a custom token manager with in-memory storage.
     Tokens are refreshed manually in each RemoteDataSourceImpl via a shared interceptor. -->

**Known issues with current approach**:
- ...
- ...

---

## To (Target State)

<!-- Describe the end state with the same level of detail. -->

**Pattern / library / approach**:

**How it should work after migration**:

**Key benefits of the new approach**:
- ...
- ...

---

## Migration Scope

<!-- What's affected? Check all that apply and add specifics. -->

### Modules Affected

- [ ] `core:{module}` — ...
- [ ] `features:{name}` — ...
- [ ] `composeApp` — ...
- [ ] `iosApp` — ...
- [ ] `build-logic` — ...
- [ ] `testing:*` — ...

### Files Affected (estimated)

| Layer | Approximate Count | Type of Change |
|-------|-------------------|----------------|
| api/ | | Interface changes |
| impl/ | | Implementation swap |
| test/ | | Fake updates |
| build files | | Dependency changes |
| config | | New/changed properties |

---

## Migration Strategy

<!-- How should this be executed? All-at-once or phased? -->

**Approach**: <!-- big-bang | phased | strangler-fig (old + new coexist) -->

### Phase Plan (if phased)

| Phase | Scope | Milestone | Can Ship Independently |
|-------|-------|-----------|----------------------|
| 1 | Introduce new abstraction alongside old | Both paths work | yes / no |
| 2 | Migrate consumers one by one | Old path has zero callers | yes / no |
| 3 | Remove old code | Clean state | yes / no |

### Coexistence Plan (if old and new overlap)

<!-- How do old and new coexist during the transition? -->

**Bridge / adapter pattern**:
<!-- e.g., NewAuthProvider wraps OldTokenManager during transition,
     delegating to old impl until each consumer is migrated. -->

**Feature flag** (if gating the switch):
| Flag Key | Default | Description |
|----------|---------|-------------|
| `use_new_{thing}` | `false` | Routes to new implementation when enabled |

---

## API / Contract Changes

<!-- Does this migration change any public contracts (api/ modules)? -->

### Changed Abstractions

| Current | New | Breaking |
|---------|-----|----------|
| `OldInterface` in `api/domain` | `NewInterface` in `api/domain` | yes / no |

### Changed Method Signatures

```
// Before
fun getData(): Flow<OldType>

// After
fun getData(): Flow<NewType>
```

### Downstream Impact

<!-- Which consumers of the changed contracts need updating? -->

| Consumer | Module | Change Required |
|----------|--------|-----------------|
| `{Feature}Presenter` | `features:{name}:impl:presentation` | Update type reference |
| `Fake{Thing}` | `features:{name}:test` | Update to new interface |

---

## Dependency Changes

<!-- Libraries being added, removed, or version-bumped. -->

### Added

| Dependency | Version | Module | Purpose |
|------------|---------|--------|---------|
| `new.library:artifact` | `1.0.0` | `core:{module}` | Replaces old approach |

### Removed

| Dependency | Module | Reason |
|------------|--------|--------|
| `old.library:artifact` | `core:{module}` | Replaced by new library |

### Version Changes

| Dependency | From | To | Reason |
|------------|------|----|--------|
| `some.lib:artifact` | `2.x` | `3.x` | Required by new approach |

---

## Data Migration (if applicable)

<!-- Does stored data (local DB, DataStore, caches) need migration? -->

**Data format changes**:

**Migration path**:
<!-- e.g., Room auto-migration from schema v3 to v4,
     or manual DataStore migration clearing old keys. -->

**Data loss risk**: <!-- none | recoverable (re-fetch) | permanent (user data) -->

**Rollback for data**: <!-- data is forward-only | can restore from backup | old format preserved -->

---

## Rollback Plan

<!-- What happens if the migration needs to be reverted? -->

**Can this be rolled back?**: <!-- yes fully | yes partially | no (one-way) -->

**Rollback mechanism**:
<!-- e.g., revert commit + redeploy, disable feature flag,
     or "cannot roll back — data migration is destructive" -->

**Rollback deadline**: <!-- point after which rollback becomes impossible, e.g., "after old API is decommissioned" -->

**Monitoring during rollout**:
- [ ] Dashboard / metric to watch: ...
- [ ] Alert threshold: ...
- [ ] Who to notify if rollback needed: ...

---

## Testing Strategy

<!-- How do you verify the migration is correct? -->

### Before Migration

- [ ] Snapshot current test results as baseline
- [ ] Add characterization tests for behavior that must be preserved

### During Migration (if phased)

- [ ] Both old and new paths covered by tests
- [ ] Feature flag toggle tested in both states
- [ ] Integration test verifying bridge/adapter works

### After Migration

- [ ] All existing tests pass with new implementation
- [ ] Old code paths have zero test coverage (confirming removal)
- [ ] Performance benchmark comparison (if applicable)

### New Test Scenarios

- [ ] ...
- [ ] ...

---

## Cleanup Checklist

<!-- What needs to be removed after migration is complete? -->

- [ ] Old implementation files
- [ ] Old dependency declarations in build.gradle.kts
- [ ] Bridge / adapter code
- [ ] Feature flag for migration toggle
- [ ] Old DTOs / mappers
- [ ] Migration-specific tests
- [ ] References in AGENTS.md files
- [ ] References in standards docs

---

## Constraints & Considerations

- **Timeline**: <!-- any deadlines driving this migration -->
- **Coordination**: <!-- other teams/features that need to be aware -->
- **iOS parity**: <!-- does iOS need equivalent changes? -->
- **Market-specific**: <!-- any markets affected differently? -->
- **Performance**: <!-- expected impact on startup, memory, network -->

---

## Out of Scope

<!-- What this migration explicitly does NOT touch. -->

- ...
- ...
