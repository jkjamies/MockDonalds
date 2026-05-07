# Ways of Working

Agent work should leave the repo easier for the next agent and the next human to understand. Use local specs for non-trivial work, keep project guidance current, and promote only the documentation that the team explicitly wants to keep.

## Local Task Specs

For non-trivial agent work, create or update a local Markdown file under `specs/` before implementation. "Non-trivial" means the work touches multiple files or platforms, changes behavior, has unclear acceptance criteria, requires staged verification, or may need to be resumed later.

`specs/` is local agent task/spec memory. It is ignored by default and should not be committed unless the user explicitly asks to make a specific spec durable. Existing tracked files under `specs/` are intentional documentation exceptions; do not add new tracked specs by accident.

Use a local spec to capture:
- user intent and acceptance criteria
- affected KMP/CMP layers and platform surfaces
- implementation status and next steps
- verification commands, results, and gaps
- decisions that should survive context compaction

### Status Values

Use one of these status values:

| Status | Meaning |
|--------|---------|
| `draft` | Requirements are still being shaped. Do not implement from this without judgment. |
| `ready` | Requirements are clear enough to implement. |
| `in_progress` | Implementation or verification is actively underway. |
| `partial` | Some work is complete, but known scope remains. |
| `blocked` | Progress requires user input, credentials, device/simulator availability, or another dependency. |
| `complete` | Implementation and required verification are done, or the user accepted the remaining gaps. |

When useful, track implementation state and verification state separately. For example, a task can be `implementation: complete` while `verification: blocked` because an Android emulator or iOS simulator is unavailable.

### Lightweight Spec Template

```markdown
# <!-- Short task name -->

**Status**: draft
**Implementation**: draft
**Verification**: not_started
**Last updated**: <!-- YYYY-MM-DD -->

## Summary
<!-- One paragraph describing the requested outcome and why it matters. -->

## Scope
<!-- List the features, core modules, app hosts, Android/iOS surfaces, and docs expected to change. -->

## Requirements And Acceptance Criteria
<!-- Prefer observable behavior. Include platform-specific expectations when Android, iOS, shared Kotlin, or Compose Multiplatform differ. -->

- [ ] <!-- Acceptance criterion -->

## Implementation Plan
<!-- Keep this lightweight. Note layer order, key files, migration concerns, and any risky assumptions. -->

## Decisions
<!-- Record decisions that should survive context compaction or handoff. -->

## Progress
<!-- Update before stopping. Mention completed edits and remaining work. -->

## Verification
<!-- Commands run, results, failures, skipped checks, and why. -->

## Next Step
<!-- The single next action another agent or human should take. -->
```

Before stopping, update the local spec's status, progress, verification, and next step. Do this even when blocked so the next agent can resume without rediscovery.

### Completion And Promotion

When work completes and used a local `specs/` file, ask the user whether to promote it into durable team documentation. If the answer is yes, invoke `create-confluence-documentation` with the `specs/` file path.

Do not publish to Confluence without explicit user confirmation of:
- destination
- title
- audience
- update mode, when an existing page may be touched
- publish intent

If confirmation or access is missing, draft cleaned Markdown for review instead of publishing.

## Contribution Workflow

1. **Branch** from `main` with a descriptive name (e.g., `feature/rewards-history`, `fix/login-redirect`)
2. **Spec** — create or update a local `specs/` file for non-trivial work; convert PM requirements using `ac-to-spec` when useful, then feed the spec to implementation skills
3. **Scaffold** using skills if adding structural elements (add-feature, add-screen, add-use-case, add-repository)
3. **Implement** business logic, UI, and tests following architecture rules in root AGENTS.md
4. **Verify** using the `verify` skill — `diff` for iterative work, `full` before pushing, `all` before opening a PR
5. **Code review** — run the code-review skill before opening a PR
6. **Merge** to main after review approval and green verification

## How to Use Skills

Skills live in `.agents/skills/` with a `SKILL.md` file each. Invoke by name.

### Verification (read-only, safe to run anytime)
| Skill | When to Use |
|-------|-------------|
| `verify` | After any code change — `diff` (default, changed modules), `full` (whole project + builds), `all` (every test level + variant) |
| `run-unit-tests` | Quick check of Kotest suite only |
| `run-ui-tests` | After UI changes (requires Android device/emulator) |
| `run-arch-tests` | After structural changes (naming, DI, module layout) |
| `run-all-tests` | Lint + all 5 test levels on both platforms |

### Scaffolding (modifies code, always verify after)
| Skill | When to Use |
|-------|-------------|
| `add-feature` | New feature module (creates all 6 submodules + tests + AGENTS.md) |
| `add-screen` | New screen in existing feature (9+ files: Screen, Presenter, UiState, Event, UI, tests) |
| `add-use-case` | New interactor (abstract in api + impl in domain + fake in test + test) |
| `add-repository` | New repository (interface in domain + impl in data + test) |

### Testing (modifies code)
| Skill | When to Use |
|-------|-------------|
| `add-unit-tests` | Fill unit test gaps identified from branch diff |
| `add-ui-tests` | Fill UI test gaps identified from branch diff |
| `add-tests` | Combined unit + UI test gap-filling |

### Spec Generation
| Skill | When to Use |
|-------|-------------|
| `ac-to-spec` | Convert PM artifacts (Gherkin, Jira, PRD) into a structured spec file for implementation skills |
| `reverse-spec` | Reverse-engineer a spec from existing code for documentation or pre-refactor baseline (read-only) |
| `create-confluence-documentation` | Promote a local `specs/` file into product or engineering Confluence documentation after explicit confirmation |

### Code Quality (read-only)
| Skill | When to Use |
|-------|-------------|
| `code-review` | Before opening a PR — diff-based review against default branch |

## Code Review Process

1. Run the `code-review` skill before requesting human review
2. Fix all issues the skill identifies
3. Human reviewers check what automation cannot:
   - Business logic correctness and edge cases
   - UX quality and consistency
   - Performance implications (unnecessary recompositions, N+1 queries)
   - Naming clarity and API design
4. All verification steps must pass before merge

## PR Standards

- Descriptive title summarizing the change (not just "Update HomePresenter")
- Link to issue/ticket if applicable
- Include verification output (or confirm all steps passed)
- Update AGENTS.md files if new conventions or patterns were introduced
- If adding a new feature, ensure its `features/{name}/AGENTS.md` was created by the scaffold skill

## Self-Updating Documentation

Agentic files (AGENTS.md, standards, skills) must stay in sync with the codebase. When your work introduces or changes conventions, update the relevant documentation as part of the same change — not as a follow-up.

**During any code change, check:**
- Did I introduce a new pattern? → Update the relevant `.agents/standards/*.md` file
- Did I add a new module or feature? → Ensure its `AGENTS.md` exists and is accurate
- Did I change a naming convention, DI pattern, or test approach? → Update root `AGENTS.md` and the relevant standard
- Did I add a new Konsist rule? → Update `testing/architecture-check/AGENTS.md` with the new test count and category
- Did I discover that existing documentation is wrong or outdated? → Fix it now

Documentation that drifts from reality is worse than no documentation — it actively misleads. Treat agentic file updates as part of the definition of done, not optional cleanup.

## When to Update Agentic Files

| Change | File to Update |
|--------|---------------|
| New feature module | `features/{name}/AGENTS.md` (created by add-feature skill) |
| New project-wide convention | Root `AGENTS.md` + relevant `.agents/standards/*.md` |
| New skill | `.agents/skills/{name}/SKILL.md` with YAML frontmatter |
| New core module | `core/{module}/AGENTS.md` |
| New architecture rule | `testing/architecture-check/AGENTS.md` (update test count and category table) |

## How to Add a New Konsist Rule

1. Create a new `BehaviorSpec` in the appropriate category under
   `testing/architecture-check/src/test/kotlin/com/mockdonalds/app/architecture-check/`
2. Use `Konsist.scopeFromProject()` for project-wide checks
3. Use `resideInPath("..impl/domain..")` for module-scoped checks
4. Use `Konsist.scopeFromSourceSet("commonMain", "features..", "domain")` for source-set-scoped checks
5. Filter with `resideInPath("..commonMain..")` to exclude test code from production rules
6. Follow the existing category structure: architecture, circuit, core, layers, testing
7. Update test count in `testing/architecture-check/AGENTS.md`
8. Run `./gradlew :testing:architecture-check:test` to validate the new rule passes

## Onboarding Checklist

1. Read the root `AGENTS.md` — architecture rules, naming conventions, forbidden patterns
2. Browse `.agents/skills/` — understand available automation
3. Run the `verify` skill on a clean checkout to confirm the environment works
4. Explore one feature module end-to-end: `api/domain` -> `api/navigation` -> `impl/domain` -> `impl/data` -> `impl/presentation` -> `test/`
5. Read `composeApp/AGENTS.md` for navigation and bridge architecture
6. Read `iosApp/AGENTS.md` for Swift-side conventions and Harmonize tests
7. Check `testing/architecture-check/AGENTS.md` for architecture enforcement categories
