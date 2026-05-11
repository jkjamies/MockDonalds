---
name: ac-to-spec
description: Convert acceptance criteria, Jira tickets, Gherkin scenarios, PRDs, or any product requirements into a structured spec template. Use when translating PM artifacts into actionable specs for add-*, update, migrate, or remove skills.
---

# AC to Spec

Translate product requirements — in any format — into a filled-in spec template that can be fed directly to implementation skills.

**Parameters**: spec type (optional — inferred from content if omitted)

**Usage examples**:
```
/ac-to-spec @jira-ticket.md                    # infer spec type from content
/ac-to-spec new @prd-excerpt.md                # explicit: new feature spec
/ac-to-spec change @gherkin-scenarios.feature   # explicit: change spec
/ac-to-spec migrate                             # paste inline, explicit type
/ac-to-spec remove @slack-thread.txt            # explicit: removal spec
```

## Spec Types

| Type | Template | When to use |
|------|----------|-------------|
| `new` | `.agents/templates/new-spec.md` | Building something that doesn't exist yet |
| `change` | `.agents/templates/change-spec.md` | Modifying, enhancing, or fixing existing behavior |
| `migrate` | `.agents/templates/migrate-spec.md` | Swapping libraries, upgrading APIs, architecture refactors |
| `remove` | `.agents/templates/remove-spec.md` | Deprecating, killing, or cleaning up existing code |

## Input Formats

The skill accepts any of these — no preprocessing required:

- **Gherkin** — `Given/When/Then` scenarios
- **Jira ticket** — title, description, acceptance criteria checklist
- **PRD prose** — product requirements document sections
- **User stories** — `As a [role], I want [action] so that [outcome]`
- **Bullet lists** — informal requirements from Slack, email, meeting notes
- **Free text** — conversational description of what's needed

The skill extracts structure from whatever is provided.

## Subagent dispatch (multi-feature grilling)

The grill step (step 7) runs inline. For specs that span **more than 3 features** (rare but real — cross-cutting changes like analytics overhauls, design-system migrations, or auth-flow rewrites), dispatch one `Explore` agent (or equivalent) IN PARALLEL with the grill so the user-facing dialog isn't blocked on serial codebase reads.

**Recommended prompt template:**

```
For a spec covering changes across {list of features}, report per feature:
- Current Key Types from `features/{feature}/AGENTS.md`.
- Recent changes that affect the spec's domain (last 5 commits to that
  feature directory).
- Cross-feature dependencies (incoming + outgoing) per its AGENTS.md.
Use the report to seed grill questions in step 7 — DO NOT modify the spec
yourself, return findings only.
```

See `.agents/standards/ways-of-working.md` → "When to Spawn Subagents". For single-feature or single-domain specs, the inline grill alone is enough — direct reads are faster than briefing a subagent.

## Steps

### 1. Accept Input

The user provides requirements via `@file` reference or inline paste. Read the full content.

### 2. Determine Spec Type

If the user specified a type (`new`, `change`, `migrate`, `remove`), use it.

If not, infer from content signals:

| Signal | Inferred type |
|--------|---------------|
| "build", "create", "add new", "introduce", new feature/screen names with no existing code | `new` |
| "change", "update", "enhance", "fix", "modify", references to existing behavior | `change` |
| "migrate", "swap", "upgrade", "replace X with Y", "move from A to B" | `migrate` |
| "remove", "deprecate", "kill", "sunset", "clean up", "delete" | `remove` |

If the signals are ambiguous or mixed, ask the user which type to use rather than guessing wrong. Frame the question with what you detected:

> The requirements mention both adding new functionality and modifying existing behavior. Should this be a `new` spec (standalone feature) or a `change` spec (enhancement to an existing feature)?

### 3. Read the Target Template

Read the appropriate template from `.agents/templates/{type}-spec.md` to understand the full structure.

### 4. Read Existing Code (for `change`, `migrate`, `remove`)

For spec types that modify existing code, read the current state to ground the spec:

- `change` — read the feature's current models, presenter, UiState, events to accurately describe "Current Behavior"
- `migrate` — read the current implementation to document "From (Current State)"
- `remove` — read the target to enumerate files, dependencies, and dependents

For `new` specs, check whether a feature with a similar name already exists — if so, flag it and confirm with the user whether this is really `new` or should be `change`.

### 5. Extract and Map Requirements

Parse the input and map extracted information onto template sections. Work through the input methodically:

**From any format, extract**:
- **What** — the feature/change/target (→ Overview, Feature name)
- **Why** — business motivation (→ Business Context)
- **Who** — user role/persona (→ User story)
- **Behaviors** — observable outcomes (→ Acceptance criteria)
- **Data** — nouns, entities, fields mentioned (→ Domain Models)
- **Actions** — verbs, operations, user interactions (→ Use Cases, Events)
- **Screens/UI** — layout descriptions, states, flows (→ Screen & UI)
- **Endpoints** — API calls, services, data sources (→ API / Network)
- **Conditions** — edge cases, error handling, gating (→ Feature Flags, Error Responses)
- **Constraints** — performance, accessibility, market-specific (→ Constraints)

**Gherkin-specific extraction**:
- `Given` clauses → preconditions, current state, test setup context
- `When` clauses → user actions → Events, Use Cases
- `Then` clauses → expected outcomes → Acceptance criteria, UiState fields, test assertions
- `And`/`But` clauses → additional conditions, edge cases
- Scenario names → test scenario descriptions
- Scenario Outlines / Examples tables → parameterized behavior, enum types

**Jira-specific extraction**:
- Title → Overview summary
- Description → Business Context
- AC checklist → Acceptance criteria (preserve as-is, plus map to technical sections)
- Labels/components → Feature name, affected modules
- Priority/story points → Constraints (if they imply scope limits)
- Linked issues → Cross-Feature Dependencies

### 6. Fill the Template

Populate every section where the input provides enough information. Follow these rules:

- **Fill confidently** — if the AC clearly describes something, map it to the right section with concrete details
- **Mark gaps for the grill (step 7)** — if the AC implies something but lacks specifics, fill what you can and tag the gap internally with `<!-- TODO: [what's missing] -->`. These markers are temporary working state; they MUST be resolved during the grill step before the spec is finalized
- **Don't leave silent gaps** — if a section has zero signal from the AC, do not skip it; tag it with `<!-- TODO: no signal in AC — [what the user needs to decide] -->` so the grill step targets it
- **Don't invent** — never fabricate endpoint paths, field names, or UI layouts that aren't grounded in the input. Tag the gap and let the grill resolve it; never guess
- **Acceptance criteria are unchecked** — AC items in the spec use `- [ ]` (unchecked), never `- [x]`. The spec describes work to be done, not work already completed
- **Preserve Out of Scope** — if the AC defines out-of-scope items, carry them into the template's Out of Scope section verbatim. This prevents scope creep during implementation
- **Preserve Constraints** — if the AC mentions constraints, technical considerations, or implementation guidance (e.g., "keep the data layer clean for future swap"), carry them into the Constraints & Considerations section
- **Use project conventions** — when filling technical sections, follow the naming patterns and architecture documented in CLAUDE.md and `.agents/standards/`. For example, use `CenterPostSubjectInteractor` for streaming use cases, the `{Name}RemoteDataSource` / `{Name}RemoteDataSourceImpl` pattern for data sources. For TestTags, read an existing feature's TestTags file to match the actual naming convention used in the codebase (e.g., PascalCase vs snake_case)
- **Preserve AC language** — keep the PM's terminology in Business Context and Acceptance Criteria sections. Translate to technical terms in the implementation sections

### 7. Grill Until Clean

Before finalizing, scan the in-progress spec for unresolved markers and resolve every one with the user. Follow the grill-me skill on the working spec:

- Scan for `<!-- TODO -->` markers, empty `- [ ]` AC items, empty required header fields, raw template placeholders, and `...` table cells
- Order branches by dependency (identity → business context → domain → API → use cases → repo → screen → cross-cutting)
- Explore the codebase before asking — confirm answers from sibling features, conventions, and existing infrastructure
- Ask one question at a time, always with a recommended answer and the tradeoff
- Write each resolution back into the spec immediately
- Append a `## Decisions` section logging every resolved question + rationale

The conversion is not done until the spec is grill-clean. **No `<!-- TODO -->` markers may survive into final output.** If the user explicitly defers a decision, log it under `### Deferred` in the Decisions section with the reason — do not leave it as a TODO.

### 8. Append Original Requirements

At the bottom of the spec, after all template sections, add a reference section preserving the original input:

```markdown
---

## Original Requirements

> **Source**: [format detected, e.g., "Gherkin scenarios", "Jira ticket", "PRD excerpt", "inline description"]
> **Converted on**: {date}
> **Spec type**: {type} (inferred / explicit)

<details>
<summary>Original acceptance criteria (click to expand)</summary>

{verbatim original input, unmodified}

</details>
```

This keeps the spec as the primary artifact while maintaining traceability to the PM's original language.

### 9. Present the Result

Output the complete spec. Tell the user:

1. Which spec type was used (and why, if inferred)
2. How many decisions were grilled (and how many, if any, were deferred — call out deferred ones explicitly)
3. Suggested next step — which skill to run with this spec (e.g., `/add-feature @specs/{name}.md`, `/update order @specs/order-change.md`). Because the grill step ran, the spec is implementation-ready; consumer skills can scaffold without further interrogation.

The user can save the output as a file (e.g., `specs/{name}-spec.md`) before feeding to the implementation skill.

## Key Rules

- **The spec is the artifact, not the AC** — the output should be immediately usable by implementation skills without referencing the original AC
- **Infer confidently, flag uncertainty** — when the content clearly points to a spec type, just use it. When ambiguous, ask
- **Grill until clean — never punt TODOs forward** — the conversion is not done while gaps remain. Every unresolved decision must be answered (or explicitly deferred with a logged reason) before the spec is finalized. `<!-- TODO -->` markers are working state for step 6 only; they MUST NOT survive into final output
- **Don't invent — grill instead** — when the AC doesn't ground a value, do not guess. Ask the user during step 7, with a recommended answer and the tradeoff
- **Stay grounded** — every filled section should trace back to either the input or a grill answer. If you can't point to a source, it's invention
- **Use project vocabulary** — this codebase has specific patterns (CenterPost, Circuit, Metro, etc.). Use them in technical sections so the spec reads natively
- **Respect the template** — don't add sections that aren't in the template or skip sections that are. Delete-if-not-applicable instructions from the template headers still apply

## No Verification Needed

This skill is read-only — it produces a document, not code changes. No verification step required.
