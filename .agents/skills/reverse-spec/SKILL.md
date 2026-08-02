---
name: reverse-spec
description: Reverse-engineer a spec from existing code — reconstructs presumed acceptance criteria, data flow, API contracts, UI states, and business rules by reading source, tests, and git history. Use when inheriting undocumented features, onboarding onto unfamiliar code, or preparing for a refactor.
---

# Reverse Spec

Read existing code and produce a filled-in spec as if the feature was being designed from scratch. The output is explicitly marked as **reverse-engineered and presumed** — it reflects what the code does today, not necessarily what was originally intended.

**Parameters**: target (feature, module, or specific component)

**Usage examples**:
```
/reverse-spec order                    # full feature → produces a new-spec.md style output
/reverse-spec order OrderPresenter     # single component deep-dive
/reverse-spec core:network             # core module
/reverse-spec order ios                # feature with iOS-specific focus
/reverse-spec order android            # feature with Android-specific focus
```

## When to Use

- **Inheriting a feature** — someone left, documentation is thin, you need to understand the implicit contract
- **Before a refactor** — reconstruct the "what" before changing the "how"
- **Onboarding** — faster than reading every file; produces a structured document you can discuss with the team
- **Validating AGENTS.md** — compare the reverse-spec against existing documentation to find drift
- **Pre-migration baseline** — capture the current behavior before a `/migrate` so you can verify equivalence after

## Pre-flight: Subagent dispatch (multi-feature reverse-engineering)

When reverse-engineering a spec from **more than 3 features** or across multiple core modules, dispatch one `Explore` agent BEFORE step 1 to map the surface in parallel. This is the canonical "open-ended exploration" case the `Explore` agent was designed for — sequential reads of 20+ files compound otherwise.

**Recommended prompt template:**

```
Map the implementation of `{feature/module}` to seed a reverse-spec. Report:
- Every file path and 1-line purpose.
- Data model (models, DTOs, mapper functions).
- Use case shapes (abstract class signature + impl method signatures).
- Presenter/UiState/Event shapes.
- Test coverage breakdown (unit / ui-component / navint / e2e).
- AGENTS.md sections and what they document.
Output a structured summary, NOT raw code dumps.
```

See `.agents/standards/ways-of-working.md` → "When to Spawn Subagents". For single-feature reverse-specs (~10 files), direct reads are usually faster than briefing a subagent.

## Information Sources

Read these in order — each layer adds context the previous one can't provide:

| Source | What it reveals |
|--------|----------------|
| `features/{name}/AGENTS.md` | Documented intent (may be stale — note discrepancies) |
| `api/domain/` source | Domain models, use case signatures — the public contract |
| `api/navigation/` source | Screen types, TestTags — navigation surface |
| `impl/domain/` source | Use case logic, repository interfaces — business rules |
| `impl/data/` source | Repository impls, data sources, DTOs, mappers — API contract |
| `impl/presentation/` source | Presenter logic, UiState, Events — UI behavior |
| `impl/presentation/` androidMain | Compose UI — visual layout and interactions |
| `iosApp/` SwiftUI views | iOS-specific UI and bridge patterns |
| `test/` fakes | Default test values — reveals expected data shapes |
| `*Test.kt` files | What's actually tested — reveals the enforced contract |
| `git log --follow` | Who built it, when, why (commit messages), how it evolved |
| `git blame` | Recency of each section — older code is more likely to be load-bearing |

## Steps

### 1. Read the Code

Read all source files for the target scope. For a full feature, read in this order:

1. `AGENTS.md` — start with documented intent
2. `api/domain/` — domain models and use case abstractions
3. `api/navigation/` — screens and test tags
4. `impl/domain/` — use case implementations and repository interfaces
5. `impl/data/` — repository impls, data sources, DTOs, mappers
6. `impl/presentation/` — presenter, UiState, events, UI
7. `test/` — fakes and their DEFAULT values
8. All `*Test.kt` files — what's actually verified

### 2. Read Git History

```bash
# Feature evolution — who, when, why
git log --oneline --follow -- features/{name}/

# Key decision points — look for large commits, refactors, renames
git log --stat --follow -- features/{name}/ | head -100

# Recent activity — is this actively maintained or abandoned?
git log --oneline -10 -- features/{name}/
```

### 3. Produce the Reverse-Spec

Output a filled-in spec using the `new-spec.md` template structure. Every section must be populated from what the code actually does — not what you think it should do.

**Critical**: Mark the entire output with this header:

```markdown
# Reverse-Engineered Spec: {Feature}

> **This spec was reverse-engineered from the existing codebase on {date}.**
> It reflects what the code does today — not necessarily what was originally
> intended. Treat as presumed acceptance criteria. Verify with the team
> before using as a source of truth.
>
> Generated by: `/reverse-spec {target}`
> Source commit: {short SHA}
```

### 4. Fill in Each Section

#### Overview
- Extract feature name, primary screen, purpose from code and AGENTS.md
- Note if AGENTS.md is missing or appears stale

#### Business Context (Presumed)
- Infer user story from presenter logic and UI
- Derive acceptance criteria from test assertions
- Flag anything that looks like it was partially implemented or abandoned

#### Domain Models
- Document every data class in `api/domain/` with all fields and types
- Note which fields are nullable and what that implies
- Note any `@Serializable` annotations (shared with DTOs)

#### API / Network
- Extract base URL config from data source impl (`appBuildConfig.{x}BaseUrl`)
- Document every endpoint from HTTP client calls (method, path, auth mode)
- Document DTO shapes from `@Serializable` classes
- Document mapper logic (DTO → domain model transformations)
- Note any TODOs or placeholder implementations

#### Use Cases
- List every abstract use case with params and result types
- Document the data flow (which repository methods are called, how flows are combined)
- Note streaming (`StrataSubjectInteractor`) vs one-shot (`StrataInteractor`)

#### Repository
- Document interface methods and return types
- Document implementation details (which data sources, caching strategy)
- Note if implementation is real (API calls) or placeholder (in-memory/flowOf)

#### Screen & UI
- Document screen type (Screen, ProtectedScreen, TabScreen, FlowScreen)
- Extract UiState fields and their types
- List all events from the sealed class
- Describe UI layout from Compose code
- Document all TestTags

#### Navigation
- Find all `navigator.goTo()` calls — incoming and outgoing
- Check for deep link registrations
- Note auth gating (ProtectedScreen)

#### Analytics
- Find any `AnalyticsEvent` sealed classes
- Document all event names and properties
- Note which events are in presenters vs domain/data

#### Feature Flags
- Find any `FeatureFlag` definitions and their corresponding `FeatureFlagDefinition` `@ContributesIntoSet` classes (capture description, owner, `FlagLifecycle`)
- Document where they're checked (presenter `rememberFlag` vs domain/data `isEnabled` / `observe`)

#### Cross-Feature Dependencies
- Grep for imports from other features' `api/` modules
- Grep for other features importing this feature's `api/`

#### Testing Coverage
- List all test files and what they cover
- Note which layers have tests and which don't
- Extract key test scenarios as implicit acceptance criteria
- **This is the most reliable source of "what's actually enforced"**

### 5. Flag Discrepancies

At the bottom of the spec, add a section:

```markdown
## Discrepancies & Observations

### Code vs Documentation
- [any differences between AGENTS.md and actual code]

### Incomplete Implementations
- [TODOs, placeholder data, commented-out code]

### Missing Test Coverage
- [layers or scenarios with no tests]

### Potential Tech Debt
- [patterns that don't match project conventions]
- [old patterns that predate current standards]

### Questions for the Team
- [things that can't be determined from code alone]
- [business logic that seems intentional but unclear why]
```

### 6. Grill Until Clean

A reverse-spec is built from inference, so it accumulates `presumably` / `appears to` / `unclear why` markers and a Questions for the Team section. Before finalizing, walk every one of these with the user. Follow the grill-me skill on the working spec, with reverse-spec-specific marker targets:

- Every `presumably` / `appears to` / `seems to` qualifier in any section
- Every entry under Discrepancies & Observations → Questions for the Team
- Every entry under Incomplete Implementations and Potential Tech Debt where intent is unclear
- Every "unclear why" or "intent not obvious" annotation

For each, present the codebase evidence, propose your best interpretation, and ask the user to confirm or correct. Write each resolution back into the spec immediately:

- Replace `presumably X` with `X` (and cite the user as the source) when confirmed
- Rewrite the section if the user corrects the inference
- Move unresolvable items to a `### Deferred` subsection with a note on what would unblock them (e.g., "needs PM input", "needs git archaeology beyond available history")

Append a `## Decisions` section above the existing `## Discrepancies & Observations` section logging every confirmation/correction with rationale. The reverse-spec is not done while presumptions remain unconfirmed.

## Output Format

The output should be a complete, grill-confirmed spec that could be saved as a file and used as input to other skills (`/update`, `/migrate`, `/add-tests`). Because the grill step ran, presumptions have been resolved against the user's actual knowledge — the spec describes what IS, not what was inferred. The user can:

1. **Read it** — understand the feature quickly
2. **Save it** — `specs/{feature}-reverse-spec.md` for reference
3. **Feed it forward** — use as input to `/update` or `/migrate` for the next change (no re-grilling needed)
4. **Compare it** — diff against AGENTS.md to find documentation drift
5. **Share it** — hand to a teammate as a briefing document

## Key Rules

- **Report what IS, not what SHOULD BE** — this is archaeology, not design
- **Cite your sources** — reference file paths and line numbers for every claim
- **Flag uncertainty during inference, then grill it out** — `presumably` / `appears to` qualifiers are working state for steps 1–5; step 6 must resolve every one with the user. The final spec should not contain unconfirmed inferences (except deferred items in the Decisions log)
- **Don't skip tests** — test assertions are the most reliable source of truth
- **Check git blame for context** — commit messages often explain "why"
- **Note staleness** — if AGENTS.md contradicts the code, the code wins
- **Grill until clean — never punt presumptions forward** — the reverse is not done while inferences remain unconfirmed. Every `presumably` / `appears to` / `unclear why` must be resolved (or explicitly deferred with a logged reason) before the spec is finalized

## No Verification Needed

This skill is read-only — it produces a document, not code changes. No verification step required.
