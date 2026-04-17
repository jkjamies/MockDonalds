# Agent Infrastructure

This directory contains automation skills, spec templates, and reference standards for AI agents working on MockDonalds.

## Standards

The `standards/` directory contains detailed reference documents covering architecture, testing, naming, DI, and more. Skills and AGENTS.md files link to these for the WHY and detailed rationale — rather than duplicating rules inline.

See the Standards Reference table in the root `AGENTS.md` for the full list.

## Templates

The `templates/` directory contains spec templates that provide structured input to skills. Users copy a template, fill in relevant sections, and feed it via `@file` reference.

| Template | Purpose | Feeds into |
|----------|---------|------------|
| `new-spec.md` | Describe something new to build | `add-feature`, `add-screen`, `add-use-case`, `add-repository`, `add-api-endpoint`, `add-analytics-events`, `add-feature-flag`, `add-monitoring` |
| `change-spec.md` | Describe a modification to existing code | `update` |
| `migrate-spec.md` | Describe a migration from one approach to another | `migrate` |
| `remove-spec.md` | Describe what to tear down | `remove` |

**Usage**: Templates are optional. All skills accept three input modes:
1. **Bare** — just the skill name + arguments (scaffold with placeholders)
2. **`@file` reference** — e.g., `/add-feature @specs/deals.md` (populate from spec)
3. **Inline description** — free text typed after the command (extract and apply)

**Generating specs from requirements**: Use `/ac-to-spec` to convert PM artifacts (Gherkin, Jira tickets, PRDs, bullet lists) into a filled-in spec template. The skill infers the template type from content or accepts an explicit type (`new`, `change`, `migrate`, `remove`).

When a spec is provided via `@file`, the skill extracts parameters (feature name, screen name, etc.) from the spec's Overview section — no need to pass them as arguments.

## Skills

### How Skills Work

Each skill is a directory under `.agents/skills/` containing a `SKILL.md` file with YAML frontmatter:

```yaml
---
name: skill-name
description: What the skill does and when to use it
---

# Instructions follow in markdown...
```

Skills are invoked by name when an agent needs to perform a specific task. The `SKILL.md` body contains step-by-step instructions the agent follows. Every skill directory must have a `SKILL.md` (Konsist-enforced).

### Skill Categories

#### Verification (read-only)
- `verify` — unified verification pipeline with three scopes: `diff` (default, changed modules), `local` (full lint + unit + arch + build), `ci` (all test levels + all variants)
- `run-unit-tests` — Kotest unit tests + iOS Swift Testing
- `run-ui-tests` — Android + iOS UI tests (requires device/simulator)
- `run-arch-tests` — Konsist + Harmonize architecture tests
- `run-all-tests` — full test pipeline (lint + all 5 test levels on both platforms)

#### Spec Generation
- `ac-to-spec` — convert acceptance criteria, Jira tickets, Gherkin, or PRDs into a structured spec template (writes spec file)
- `reverse-spec` — reverse-engineer a spec from existing code (presumed AC, data flow, contracts) (read-only)

#### Code Quality (read-only)
- `code-review` — diff-based review against default branch
- `lint-branch` — fast pre-commit lint (Detekt + SwiftLint on changed files only)
- `find-dead-code` — surface unused declarations, orphaned TestTags/Screens/Fakes (accepts optional module scope)
- `summarize` — project/feature/module overview with android/ios platform scope

#### Profiling (read-only)
- `profile` — Perfetto/Macrobenchmark (Android) + Instruments (iOS) benchmarking and tracing

#### Test Generation (modifies code)
- `add-unit-tests` — fill unit test gaps from branch diff
- `add-ui-tests` — fill UI test gaps from branch diff
- `add-tests` — combined unit + UI gap-filling

#### Scaffolding (modifies code)
- `add-feature` — scaffold complete feature module (6 submodules + tests + AGENTS.md)
- `add-screen` — add screen to existing feature (9+ files)
- `add-use-case` — add interactor (abstract + impl + fake + test)
- `add-repository` — add repository (interface + impl + test + data sources + DTOs)
- `add-core-module` — scaffold core module with api/impl split
- `add-api-endpoint` — wire feature to backend API (data source + DTO + mapper + client config)
- `add-analytics-events` — add analytics event definitions + presenter/domain wiring
- `add-feature-flag` — add feature flag definition + observation + gating
- `add-monitoring` — add observability instrumentation (shell — core:monitoring not yet implemented)
- `add-config-field` — add compile-time field to `core:build-config`
- `validate-all-markets` — enforce build-config schema/format rules across all market properties

#### Modification (modifies code)
- `update` — modify existing code across all affected layers
- `remove` — clean teardown across layers with dependency analysis
- `migrate` — cross-cutting migration (library swap, pattern change, API version upgrade)

### Creating New Skills

1. Create a directory under `.agents/skills/{skill-name}/`
2. Add a `SKILL.md` with YAML frontmatter (`name` and `description` fields)
3. Write step-by-step instructions in the markdown body
4. Add a Context section documenting `@file` and inline input support
5. If the skill modifies code, include a Post-Change Verification section:
   ```
   ## Post-Change Verification
   1. ./gradlew :testing:architecture-check:test
   2. ./gradlew testAndroidHostTest
   ```
6. Reference existing code as templates rather than embedding full file contents
7. The skill will be auto-discovered — Konsist enforces that every skill directory has a `SKILL.md`
