# Ways of Working

## Contribution Workflow

1. **Branch** from `main` with a descriptive name (e.g., `feature/rewards-history`, `fix/login-redirect`)
2. **Scaffold** using skills if adding structural elements (add-feature, add-screen, add-use-case, add-repository)
3. **Implement** business logic, UI, and tests following architecture rules in root AGENTS.md
4. **Verify** using the full 6-step pipeline: detekt, unit tests, konsist, harmonize, swiftlint, assemble
5. **Code review** — run the code-review skill before opening a PR
6. **Merge** to main after review approval and green verification

## How to Use Skills

Skills live in `.agents/skills/` with a `SKILL.md` file each. Invoke by name.

### Verification (read-only, safe to run anytime)
| Skill | When to Use |
|-------|-------------|
| `verify` | After any code change, before declaring work complete |
| `verify-smart` | During iterative development on 1-2 modules (faster than full verify) |
| `run-unit-tests` | Quick check of Kotest suite only |
| `run-ui-tests` | After UI changes (requires Android device/emulator) |
| `run-arch-tests` | After structural changes (naming, DI, module layout) |
| `run-all-tests` | Lint + unit + arch tests (no build step) |

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
- Did I add a new Konsist rule? → Update `konsist/AGENTS.md` with the new test count and category
- Did I discover that existing documentation is wrong or outdated? → Fix it now

Documentation that drifts from reality is worse than no documentation — it actively misleads. Treat agentic file updates as part of the definition of done, not optional cleanup.

## When to Update Agentic Files

| Change | File to Update |
|--------|---------------|
| New feature module | `features/{name}/AGENTS.md` (created by add-feature skill) |
| New project-wide convention | Root `AGENTS.md` + relevant `.agents/standards/*.md` |
| New skill | `.agents/skills/{name}/SKILL.md` with YAML frontmatter |
| New core module | `core/{module}/AGENTS.md` |
| New architecture rule | `konsist/AGENTS.md` (update test count and category table) |

## How to Add a New Konsist Rule

1. Create a new `BehaviorSpec` in the appropriate category under
   `konsist/src/test/kotlin/com/mockdonalds/app/konsist/`
2. Use `Konsist.scopeFromProject()` for project-wide checks
3. Use `resideInPath("..impl/domain..")` for module-scoped checks
4. Use `Konsist.scopeFromSourceSet("commonMain", "features..", "domain")` for source-set-scoped checks
5. Filter with `resideInPath("..commonMain..")` to exclude test code from production rules
6. Follow the existing category structure: architecture, circuit, core, layers, testing
7. Update test count in `konsist/AGENTS.md`
8. Run `./gradlew :konsist:test` to validate the new rule passes

## Onboarding Checklist

1. Read the root `AGENTS.md` — architecture rules, naming conventions, forbidden patterns
2. Browse `.agents/skills/` — understand available automation
3. Run the `verify` skill on a clean checkout to confirm the environment works
4. Explore one feature module end-to-end: `api/domain` -> `api/navigation` -> `impl/domain` -> `impl/data` -> `impl/presentation` -> `test/`
5. Read `composeApp/AGENTS.md` for navigation and bridge architecture
6. Read `iosApp/AGENTS.md` for Swift-side conventions and Harmonize tests
7. Check `konsist/AGENTS.md` for architecture enforcement categories
