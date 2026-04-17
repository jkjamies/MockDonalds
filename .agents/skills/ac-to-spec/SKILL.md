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
- **Fill partially** — if the AC implies something but lacks specifics, fill what you can and add `<!-- TODO: [what's missing and why the implementer needs to decide] -->` markers
- **Leave as TODO** — if a section has zero signal from the AC, keep the template's placeholder structure with `<!-- TODO: no signal in AC — [what to consider] -->` so the implementer knows to fill it
- **Don't invent** — never fabricate endpoint paths, field names, or UI layouts that aren't grounded in the input. It's better to leave a TODO than to guess wrong
- **Use project conventions** — when filling technical sections, follow the naming patterns and architecture documented in CLAUDE.md and `.agents/standards/`. For example, use `CenterPostSubjectInteractor` for streaming use cases, the `{Name}RemoteDataSource` / `{Name}RemoteDataSourceImpl` pattern for data sources, etc.
- **Preserve AC language** — keep the PM's terminology in Business Context and Acceptance Criteria sections. Translate to technical terms in the implementation sections

### 7. Append Original Requirements

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

### 8. Present the Result

Output the complete spec. Tell the user:

1. Which spec type was used (and why, if inferred)
2. Which sections were filled vs left as TODO
3. Suggested next step — which skill to run with this spec (e.g., `/add-feature @specs/{name}.md`, `/update order @specs/order-change.md`)

The user can save the output as a file (e.g., `specs/{name}-spec.md`) and refine before feeding to an implementation skill.

## Key Rules

- **The spec is the artifact, not the AC** — the output should be immediately usable by implementation skills without referencing the original AC
- **Infer confidently, flag uncertainty** — when the content clearly points to a spec type, just use it. When ambiguous, ask
- **Don't over-fill** — a TODO marker is more valuable than a wrong guess. The implementer will fill gaps with domain knowledge you don't have
- **Stay grounded** — every filled section should trace back to something in the input. If you can't point to the source, it's invention
- **Use project vocabulary** — this codebase has specific patterns (CenterPost, Circuit, Metro, etc.). Use them in technical sections so the spec reads natively
- **Respect the template** — don't add sections that aren't in the template or skip sections that are. Delete-if-not-applicable instructions from the template headers still apply

## No Verification Needed

This skill is read-only — it produces a document, not code changes. No verification step required.
