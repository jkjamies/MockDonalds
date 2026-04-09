# Agent Infrastructure

This directory contains automation skills and reference standards for AI agents working on MockDonalds.

## Standards

The `standards/` directory contains detailed reference documents covering architecture, testing, naming, DI, and more. Skills and AGENTS.md files link to these for the WHY and detailed rationale — rather than duplicating rules inline.

See the Standards Reference table in the root `AGENTS.md` for the full list.

## Skills

## How Skills Work

Each skill is a directory under `.agents/skills/` containing a `SKILL.md` file with YAML frontmatter:

```yaml
---
name: skill-name
description: What the skill does and when to use it
---

# Instructions follow in markdown...
```

Skills are invoked by name when an agent needs to perform a specific task. The `SKILL.md` body contains step-by-step instructions the agent follows.

## Skill Categories

### Verification (read-only)
- `verify` — full pipeline (build + lint + all tests)
- `verify-smart` — diff-aware scoped verification
- `run-unit-tests` — Kotest unit tests
- `run-ui-tests` — Android UI tests (requires device)
- `run-arch-tests` — Konsist + Harmonize architecture tests
- `run-all-tests` — lint + unit + arch tests

### Code Quality
- `code-review` — diff-based review against default branch

### Test Generation (modifies code)
- `add-unit-tests` — fill unit test gaps from branch diff
- `add-ui-tests` — fill UI test gaps from branch diff
- `add-tests` — combined unit + UI gap-filling

### Scaffolding (modifies code)
- `add-feature` — scaffold complete feature module
- `add-screen` — add screen to existing feature
- `add-use-case` — add interactor (abstract + impl + fake + test)
- `add-repository` — add repository (interface + impl + test)

## Creating New Skills

1. Create a directory under `.agents/skills/{skill-name}/`
2. Add a `SKILL.md` with YAML frontmatter (`name` and `description` fields)
3. Write step-by-step instructions in the markdown body
4. If the skill modifies code, include a Post-Change Verification section:
   ```
   ## Post-Change Verification
   1. ./gradlew :konsist:test
   2. ./gradlew testAndroidHostTest
   ```
5. Reference existing code as templates rather than embedding full file contents
