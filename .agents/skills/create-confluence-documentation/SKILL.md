---
name: create-confluence-documentation
description: Converts a local specs/... file into readable product or engineering Confluence documentation. Use when promoting completed local agent task/spec memory into durable team docs after explicit user confirmation.
---

# Create Confluence Documentation

Promote a local `specs/...` file into durable Confluence documentation. This skill cleans agent task memory into readable product or engineering docs, then publishes only after explicit confirmation.

## Hard Stops

Stop immediately and do not publish when:
- no local spec path is provided
- the spec path is outside `specs/`
- the spec is unreadable
- no Confluence destination is provided
- publish confirmation is missing
- the Atlassian MCP/API is unavailable or unauthenticated
- an existing page update is possible but the user's update intent is ambiguous
- the target existing page appears unrelated to the spec
- the target existing page materially conflicts with the spec

When publishing is blocked, draft cleaned Markdown for user review and clearly state what confirmation, destination, or access is missing.

## Required Inputs

Collect these before any publish attempt:
- `specPath`: local path under `specs/`
- `destination`: preferred as a Confluence space URL or parent page URL
- `title`: final page title
- `audience`: product, engineering, QA, support, leadership, or another explicit audience
- `publishIntent`: explicit yes/confirm from the user
- `updateMode`: required if an existing page may be touched

Preferred destination input is a Confluence space or parent page URL. If the user cannot provide that, ask for site, space key/name, and parent page title or ID.

Existing-page update modes are:
- `append`: add a new dated section without rewriting existing content
- `merge`: reconcile the spec into the existing structure while preserving relevant content
- `replace`: overwrite the existing page body
- `create_new`: leave the existing page untouched and create a new page
- `cancel`: stop without publishing

## Workflow

1. Read the provided `specs/...` file.
2. Identify whether the output should be product documentation, engineering documentation, or a mixed implementation note based on the spec content and audience.
3. Resolve the Confluence destination. Prefer a parent page URL or space URL; otherwise use site, space key/name, and parent page title or ID.
4. Search/read the target parent and any same-title existing page before publishing.
5. If a same-title page exists, require an explicit `updateMode`.
6. For existing-page updates, compare the target page to the spec:
   - hard stop if the page is unrelated
   - hard stop if it materially conflicts with the spec
   - proceed only when the update mode and publish intent are explicit
7. Draft cleaned Markdown using the template below. Remove agent-only status bookkeeping unless it is useful to the audience.
8. Ask for final confirmation that includes destination, title, audience, update mode when applicable, and publish intent.
9. Publish through the Atlassian MCP/API only after confirmation.
10. Report the Confluence page URL, or provide the cleaned Markdown draft if blocked.

Before calling Atlassian MCP tools, read the relevant MCP tool descriptor/schema from the enabled Atlassian MCP server and authenticate if required.

## Documentation Template

Use these headings. Keep Markdown comments as authoring guidance only; do not publish visible placeholder text.

```markdown
# <!-- Final Confluence page title -->

## Summary
<!-- One-paragraph overview of the feature, change, or decision. -->

## Background
<!-- Why this exists, user/business context, and relevant history. -->

## Goals
<!-- Intended outcomes and success criteria. -->

## Out Of Scope
<!-- Explicit exclusions, deferred work, and non-goals. -->

## Requirements And Acceptance Criteria
<!-- Observable requirements. Preserve checkboxes only when useful for the audience. -->

## Non-Functional Requirements
<!-- Performance, accessibility, security, localization, reliability, privacy, compatibility, or platform constraints. -->

## Refactor / Technical Debt
<!-- Cleanup, migrations, known compromises, or follow-up refactors. -->

## Decisions
<!-- Decisions made, tradeoffs, rejected options, and rationale. -->

## Implementation Notes
<!-- Architecture, KMP/CMP layering, Android/iOS details, data flow, files/modules, or rollout notes. -->

## Risks And Dependencies
<!-- External dependencies, blockers, risky assumptions, operational concerns, or owner handoffs. -->

## Verification
<!-- What was tested, what passed, what was skipped, and remaining validation gaps. -->

## Open Follow-Ups
<!-- Work not completed, future tickets, monitoring needs, or documentation gaps. -->
```

## Cleanup Rules

- Convert agent scratch notes into prose useful to the named audience.
- Preserve durable decisions, acceptance criteria, platform contracts, and verification results.
- Remove stale status values, internal todo chatter, and implementation minutiae that do not help the audience.
- Keep KMP/CMP distinctions clear: shared Kotlin business logic, Android Compose UI, native SwiftUI on iOS, and Compose runtime on iOS only when relevant.
- If the spec says work is partial or blocked, document that honestly in `Verification` or `Open Follow-Ups`.
