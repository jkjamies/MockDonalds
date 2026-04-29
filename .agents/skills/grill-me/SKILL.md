---
name: grill-me
description: Interview the user one question at a time to resolve every unfilled marker in a spec file (`<!-- TODO -->`, empty `- [ ]` AC items, missing required fields, raw template placeholders, unconfirmed reverse-spec presumptions). Use on hand-authored specs, stale specs, or when the user explicitly says "grill me" or "stress-test this plan". The conversion skills (`/ac-to-spec`, `/reverse-spec`) embed this same logic inline, so freshly-converted specs do not need a separate grill pass. Walks the decision tree branch-by-branch, recommends an answer for each question, explores the codebase instead of asking when it can, and writes resolved answers back into the spec with a Decisions log appended.
---

# Grill Me

Stress-test a filled spec file by walking every unresolved decision branch with the user, one question at a time, until the spec is implementation-ready.

The spec is the input AND the output. This skill mutates the spec file in place.

**Parameters**: `@spec-file.md` (required)

**Usage examples**:
```
/grill-me @specs/deals.md                  # interview on every unresolved marker
/grill-me @specs/order-change.md           # works on any spec type (new/change/migrate/remove)
```

## When to Use

| Trigger | Why |
|---------|-----|
| Hand-authored spec from a template | Catch unfilled placeholders before implementation |
| Stale spec — previously grilled, but the world changed (new requirement, new constraint, schema drift) | Re-resolve only the now-stale sections |
| User says "grill me" / "stress-test this plan" / "poke holes" | Explicit invocation |
| Consumer skill pre-flight detected unresolved markers | Safety net for specs that bypassed the conversion skills |

**Note**: `/ac-to-spec` and `/reverse-spec` embed this same grill logic inline. Freshly converted specs come out grill-clean and do not need a separate `/grill-me` pass — running it on a clean spec is a no-op.

## When NOT to Use

- Spec is fully filled with no markers — there's nothing to grill
- User wants exploratory discussion, not decision-resolution — use chat instead
- Code is already written — use `/reverse-spec` to derive a spec from the code

## Steps

### 1. Read the Spec

Read the file the user passed via `@`. If no file is passed, ask which spec to grill — do not invent one.

Detect the spec type from the H1 (e.g., `# New — ...`, `# Change — ...`, `# Migrate — ...`, `# Remove — ...`) and read the matching template from `.agents/templates/{type}-spec.md` so you know which sections are required vs delete-if-not-applicable.

### 2. Scan for Unresolved Markers

Walk the spec top-to-bottom and collect every unresolved item. These are the grilling targets:

| Marker | Meaning | Example |
|--------|---------|---------|
| `<!-- TODO: ... -->` | `ac-to-spec` flagged a gap | `<!-- TODO: no signal in AC — pick a default -->` |
| `<!-- ... -->` (placeholder prose from the raw template) | Section never filled | `<!-- One-paragraph summary of what's being built and why it exists. -->` |
| `- [ ]` with no body text after it | Empty AC checkbox | `- [ ]` |
| `**Field**: ` with nothing after the colon | Missing required header field | `**Skill target**: ` |
| Table cells containing `...` or `{placeholder}` from the raw template | Unfilled table row | `\| `...` \| `...` \|` |
| Whole sections still verbatim from the template (e.g., the example `{Feature}Content` block) | Section copied but never customized | unchanged code-fence example |

Build an ordered list of these markers. Order matters — see step 3.

### 3. Order the Branches

Resolve dependencies before dependents. Rough priority:

1. **Identity** — Name, primary screen, skill target (everything else hangs off these)
2. **Business context** — User story, acceptance criteria (drives all technical decisions)
3. **Domain models** — shapes that flow through the feature
4. **API / data sources** — backend contract
5. **Use cases** — business logic
6. **Repository** — data layer
7. **Screen & UI** — presentation
8. **Navigation** — entry/exit
9. **Analytics, feature flags, build config** — cross-cutting
10. **Cross-feature dependencies, testing notes** — integration concerns

Within a section, resolve required fields before optional ones. Skip sections the template marks as "delete if not applicable" if the user confirms they don't apply.

### 4. Explore Before Asking

For each unresolved marker, decide: **can the codebase answer this?**

If yes, explore and propose the answer instead of asking blind:

| Marker | Look here |
|--------|-----------|
| `**Base URL config field**` | Read `core/build-config/api/.../AppBuildConfig.kt` for existing fields |
| TestTags naming convention | Read an existing feature's `{Feature}TestTags.kt` |
| Use case naming | Grep for existing `CenterPostSubjectInteractor` / `CenterPostInteractor` subclasses |
| Cross-feature dependencies | Read the target feature's `api/domain` and `api/navigation` modules |
| Screen type (Screen vs ProtectedScreen vs FlowScreen vs TabScreen) | Read existing screens with similar auth/flow patterns |
| Repository return types | Read sibling features' repositories |
| Feature flag lifecycle conventions | Read `core/remote-config/api/.../FeatureFlagDefinition.kt` (FlagLifecycle enum) and existing flags |
| Convention plugin to use | Read `build-logic/convention/.../*.kt` and CLAUDE.md plugin table |

When you find a grounded answer, present it as a confirmation rather than a question:

> Looked at `core/build-config/api/.../AppBuildConfig.kt`. There's no `dealsBaseUrl` field yet — I recommend adding it via `/add-config-field` before `/add-feature`. Sound good, or do you want to reuse an existing base URL?

If the codebase doesn't answer it (subjective product/design decision, future intent, business priority), ask the user.

### 5. Interview — One Question at a Time

For each remaining unresolved marker:

1. **State the context** — which section, why this matters, what depends on it
2. **Recommend an answer** — your default based on conventions, similar features, the AC
3. **Surface the tradeoff** — one sentence on what changes if they pick differently
4. **Wait for the answer** — do not batch questions. Do not move on until this branch is resolved

Question template:

> **[Section] — [Field]**
> [1-sentence why this matters and what it blocks]
>
> **Recommendation**: [your answer]
> **Tradeoff**: [what changes if they pick differently]
>
> Accept, or override?

Keep questions tight. If the user says "your call" / "you pick" / "go with the recommendation", accept the recommendation and move on.

### 6. Write Back As You Go

After each resolved question, immediately update the spec file:

- Replace the marker with the resolved answer
- Use the Edit tool with the exact marker as `old_string` (markers are unique enough; if not, include surrounding context)
- Do not batch writes — resolve one marker, write it, move to the next

This means the spec is always in a valid intermediate state. If the user interrupts the grill, the resolved answers are already persisted.

### 7. Append a Decisions Log

When all markers are resolved (or the user explicitly defers remaining ones), append a `## Decisions` section to the spec, just before the existing `## Original Requirements` section (if present, from `ac-to-spec`):

```markdown
---

## Decisions

> Resolved during `/grill-me` on {date}. Each entry: question → answer → rationale.

### [Section name]

- **[Field]** — [answer]
  - **Rationale**: [why this answer; cite codebase evidence or user input]
  - **Tradeoff accepted**: [what was given up, if relevant]

### [Next section]

- ...
```

If the user deferred any markers, list them in a `### Deferred` subsection with a note on why and what would unblock them. Do not silently leave deferred markers in place — call them out.

### 8. Final Pass

After the decisions log is written:

1. Re-scan the spec for any markers you missed (run step 2 again)
2. If any remain and weren't deferred, surface them and resume grilling
3. When clean, summarize:
   - How many decisions were resolved
   - How many were deferred (and why)
   - Suggested next step — which `add-*` / `update` / `migrate` / `remove` skill to run with the now-grilled spec

## Stop Condition

The grill stops when **all unresolved markers are either resolved (written into the spec) or explicitly deferred (logged in the Decisions section)**.

This is mechanical, not vibes-based. If the scan in step 2 returns an empty list (excluding deferred entries), the grill is done.

## Key Rules

- **One question at a time** — never batch. Walking the tree depth-first prevents context-switching and lets each answer inform the next question
- **Explore before asking** — every question should first be tested against "can I answer this from the codebase?" If yes, propose; if no, ask
- **Always recommend** — never ask an open-ended question without a recommended answer. The user's job is to accept, override, or defer — not to design from scratch
- **Write as you go** — persist each resolution to the spec immediately. Don't batch writes; the spec is always the source of truth
- **The spec is the artifact** — the grill enriches the spec, it doesn't produce a separate transcript. The Decisions log lives inside the spec
- **Don't drift** — stay on the unresolved-marker list. If the user wants to discuss something tangential, note it as a deferred decision and return to the next marker
- **Honor deferral** — if the user says "skip this, I'll figure it out later", log it as deferred with their reason. Don't grill on deferred items again in the same session
- **Don't re-decide** — if a marker has already been answered (filled text exists), don't grill on it. Only target genuine unfilled markers
- **Respect the template** — never grill on sections marked "delete if not applicable" without first asking whether the section applies at all
- **No invention** — the grill resolves decisions the user makes. It does not make up endpoint paths, field names, or business rules. When the user genuinely doesn't know, defer rather than guess

## No Verification Needed

This skill is read/write on a single document — it does not modify code. No `verify` step required after running it. The next skill in the chain (`add-feature`, `update`, etc.) will run its own verification.
