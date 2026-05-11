# Ways of Working

## Contribution Workflow

1. **Branch** from `main` with a descriptive name (e.g., `feature/rewards-history`, `fix/login-redirect`)
2. **Spec** (optional) — convert PM requirements into a structured spec using `ac-to-spec`, then feed the spec to implementation skills
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

### Code Quality (read-only)
| Skill | When to Use |
|-------|-------------|
| `code-review` | Before opening a PR — diff-based review against default branch |

## When to Spawn Subagents

This codebase's agentic infrastructure assumes any AI tool with a subagent/spawning capability (Claude Code's `Agent` tool, Codex sub-tasks, Aider's `architect` mode, Copilot agent's task forks, etc.). Subagents protect the main agent's context and parallelize work that otherwise serializes through many `Read` + `grep` calls. They have overhead — briefing them takes tokens, and they return summaries (not raw content) — so use them when the round-trip pays off.

**Dispatch when one of these triggers fires:**

| Trigger | What to spawn | Why |
|---------|---------------|-----|
| **Cross-layer change touching >3 modules** (`update`, `add-feature`, `migrate`) | One `Explore` (or equivalent) agent — pre-flight surface mapping: affected files, conventions in similar features, every consumer of removed/changed types | One round-trip beats ~10 sequential reads; preserves main context for the edit-verify loop |
| **`verify diff` failures across ≥3 modules** | One `general-purpose` agent per affected module (parallel) | Failures across modules often share one root cause that's only visible when seen together; parallel investigation cuts wall time |
| **Repository-wide reference hunt** (≥3 sequential greps for the same symbol/API/test tag, typically after a rename or removal) | One `general-purpose` agent — "find every reference to {X} across .kt, .swift, .gradle.kts, .md" | One comprehensive sweep replaces 3+ grep-then-decide cycles |
| **Cross-tool infrastructure investigation** (Xcode/Gradle/test plan/build config diagnosis) | One `general-purpose` agent — open-ended question with reproduction context | Multi-tool diagnosis benefits from focused depth while the main agent keeps moving on the primary task |

**Anti-patterns — do NOT dispatch for:**

- **Trivial single-file work.** `Read` + `Edit` of one known file is faster direct than briefing a subagent.
- **The file-edit-verify-iterate loop itself.** Main-thread continuity matters there — the subagent loses test output context between iterations.
- **Anything where you'd just re-grep the subagent's answer.** If you need raw content (not a summary), use `Read` / `Bash grep` directly.
- **Cosmetic or single-call confirmation reads.** If 1–2 targeted reads will do, skip the subagent overhead.

**The general principle:** subagents own *pre-flight context gathering* and *parallel investigations*. The main agent owns the *edit-verify-iterate loop*. When uncertain, ask: "Would a single round-trip with one subagent replace ≥3 sequential reads or greps I'm about to do?" If yes, dispatch.

Per-skill prescriptions live in each skill's `SKILL.md` — search for "Pre-flight: Subagent dispatch" or "Subagent dispatch" sections.

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
