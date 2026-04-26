---
name: publish-vault
description: Publish project markdown docs as derived Obsidian-style notes organized into a Confluence-style nested taxonomy (Home / Orientation / Standards / Modules / Skills & Tooling) with wikilinks and auto-generated index notes. Walks the current working directory, detects known doc conventions (AGENTS.md, CLAUDE.md, .agents/, docs/, README.md, ARCHITECTURE.md, CONTRIBUTING.md), and writes per-file derived notes (condensed / expanded / reframed depending on source type) to a configured Obsidian vault path. Idempotent via content-hash manifest — only re-derives changed sources on re-run. Use when you want a navigable Obsidian knowledge base for a project, or to refresh an existing vault after docs change. Personal-machine skill — vault path is configured locally.
---

> **TEMPLATE — NOT AN ACTIVE SKILL.** This file is a checked-in copy you install onto your machine. Until you copy it to `~/.claude/skills/publish-vault/` AND create your personal `config.json` (see `INSTALL.md` next to this file), `/publish-vault` will not run on your machine. **The `config.json` step is a per-developer TODO** — it contains your personal vault path; it is NOT shipped in the repo and never should be.

# Publish Vault

Project docs → derived Obsidian notes organized into a nested folder taxonomy with wikilinks and indexes. Personal, machine-local, project-agnostic.

**Parameters**: `<vault-path>` (optional — falls back to config), `--dry-run` (preview without writing), `--flat` (use legacy flat-at-root layout instead of nested taxonomy)

**Usage examples**:
```
/publish-vault                                      # uses config-default vault, derives current cwd
/publish-vault ~/Obsidian/Work                      # explicit vault path
/publish-vault --dry-run                            # show what would change, write nothing
/publish-vault ~/Obsidian/Work --dry-run            # explicit + dry-run
/publish-vault --flat                               # legacy flat layout (single-folder vault)
```

## Configuration

**REQUIRED**: each developer creates their own `~/.claude/skills/publish-vault/config.json` after installing this skill. The skill refuses to run if no config is present and no vault path is supplied via CLI. See `INSTALL.md` next to this file for the bootstrap steps.

Example (the values shown are placeholders — fill in your own):

```json
{
  "default_vault_path": "/path/to/your/Obsidian/Projects",
  "use_project_subfolder": true,
  "vault_layout": "nested",
  "include_globs": ["AGENTS.md", "CLAUDE.md", ".agents/**/*.md", ".claude/**/*.md", "docs/**/*.md", "README.md", "ARCHITECTURE.md", "CONTRIBUTING.md"],
  "exclude_globs": [".git/**", "node_modules/**", "build/**", ".gradle/**", "Pods/**", "DerivedData/**", "dist/**", "target/**", "vendor/**", ".venv/**", "__pycache__/**"],
  "prompt_version": 2,
  "taxonomy_overrides": {}
}
```

`use_project_subfolder` (default `true`): the configured `default_vault_path` points at a parent directory that holds one folder per project. The skill writes to `<default_vault_path>/<cwd-basename>/` so multiple projects can coexist without collision. The project subfolder is created on first run.

When `use_project_subfolder` is `false`, the skill writes flat into `default_vault_path` (single-project vaults).

`vault_layout` (default `"nested"`): controls folder organization inside the project subfolder.
- `"nested"` — Confluence-style numbered folders (see "Vault Layout" below). Recommended for vaults that need to scale.
- `"flat"` — every note at the project subfolder root. Use for very small projects (<20 notes) or personal-graph-only vaults.

`prompt_version` is bumped intentionally to force full re-derivation when the derivation prompts in this skill change. Hash drift alone (source unchanged, prompt changed) won't trigger re-derivation without this bump. Bumping `vault_layout` from `"flat"` to `"nested"` (or vice versa) does NOT auto-bump prompt_version, but does trigger a one-time reorganization step on next run.

`taxonomy_overrides` (optional): per-source-type folder mapping that overrides the defaults below. See "Vault Layout".

## Vault Layout

The default `nested` layout uses a numbered folder taxonomy. Numeric prefixes drive the sort order in the Obsidian file explorer. Folder names are project-agnostic and chosen to match a "Confluence engineering space" mental model.

```
<project>/
  00 — Vault Home/
    <Project> Vault.md                 (top-level entry note)
  01 — Orientation/
    <Project> Project Orientation.md   (from CLAUDE.md)
    <Project> README.md                (from README.md)
    Project (AGENTS).md                (from root AGENTS.md)
    Agents Folder (AGENTS).md          (from .agents/AGENTS.md if present)
    Project Orientation Index.md
    Project Docs Index.md
  02 — Standards/
    Standards Index.md
    General/
      Architecture.md
      Code Style.md
      ...
    Testing/
      Testing.md
      Testing Unit.md
      Verification.md
      ...
  03 — Modules/
    AGENTS Index.md
    Core/                              (per-core/* AGENTS notes)
    Features/                          (per-features/* AGENTS notes)
    Shells/                            (composeApp, androidApp, iosApp, build-logic, etc.)
    Testing Modules/                   (per-testing/* AGENTS notes)
  04 — Skills & Tooling/
    Skills Index.md
    Templates Index.md
    Skills/                            (per-SKILL.md notes)
    Templates/                         (per-template notes)
```

### Folder placement rules

For each derived note, decide its target folder using these rules in order. Stop at the first match.

1. **Vault home note** (`<Project> Vault.md`) → `00 — Vault Home/`
2. **`claude-md`** (project orientation) → `01 — Orientation/`
3. **`project-doc`** for repo-root files (`README.md`, `ARCHITECTURE.md`, `CONTRIBUTING.md`) → `01 — Orientation/`
4. **`project-doc`** for other docs (`docs/**/*.md`) → `01 — Orientation/` if it acts as orientation, otherwise `04 — Skills & Tooling/` if it documents tooling. Default: `01 — Orientation/`
5. **`agents-md`** at repo root or under `.agents/` → `01 — Orientation/`
6. **`agents-md`** under `core/<name>/` → `03 — Modules/Core/`
7. **`agents-md`** under `features/<name>/` → `03 — Modules/Features/`
8. **`agents-md`** under `testing/<name>/` → `03 — Modules/Testing Modules/`
9. **`agents-md`** under shell-like paths (`composeApp/`, `androidApp/`, `iosApp/`, `build-logic/`, `build-tooling/`) → `03 — Modules/Shells/`
10. **`standard`** with filename matching `testing*.md` or `verification.md` → `02 — Standards/Testing/`
11. **`standard`** otherwise → `02 — Standards/General/`
12. **`skill`** → `04 — Skills & Tooling/Skills/`
13. **`template`** → `04 — Skills & Tooling/Templates/`
14. **Index notes** → at the top level of their associated section:
    - `Project Orientation Index.md`, `Project Docs Index.md` → `01 — Orientation/`
    - `Standards Index.md` → `02 — Standards/`
    - `AGENTS Index.md` → `03 — Modules/`
    - `Skills Index.md`, `Templates Index.md` → `04 — Skills & Tooling/`
15. **`other-md`** → `01 — Orientation/`

A user can override any placement via `taxonomy_overrides` in config. Map keys are source-type strings or specific filenames; values are folder paths relative to the vault root.

### Empty folders

If a project has no `core/` modules, the `03 — Modules/Core/` folder is simply not created. The skill creates folders lazily as it writes notes into them.

## Source Detection

Walk the current working directory. Match each file against the include/exclude globs. Classify into one of these source types — the type determines the derivation prompt and the folder placement:

| Source Type | Match Rule | Derivation Style |
|-------------|------------|------------------|
| `claude-md` | `CLAUDE.md` at repo root | Reframe as project orientation note |
| `agents-md` | Any `AGENTS.md` (per-feature/per-module) | Condense to feature essentials, key types, dependencies |
| `standard` | `**/standards/*.md`, `**/.agents/standards/*.md` | Expand acronyms, add cross-context, surface rationale |
| `skill` | `**/.agents/skills/*/SKILL.md`, `**/.claude/skills/*/SKILL.md` | Reframe as "what it does / when to invoke / what it produces" |
| `template` | `**/.agents/templates/*.md`, `**/templates/*.md` | Condense to purpose + slot list |
| `project-doc` | `README.md`, `ARCHITECTURE.md`, `CONTRIBUTING.md`, `docs/**/*.md` | Reframe for personal-knowledge consumption |
| `other-md` | Anything else under include globs | Treat as `project-doc` by default |

Skip files matching the exclude globs even if they match an include.

Skip `.claude/projects/**` (machine-local auto-memory store) and follow-but-dedupe symlinks (e.g., `.claude/skills` → `.agents/skills`).

## Steps

### 1. Resolve Vault Path

- Use the CLI arg if provided
- Otherwise read `default_vault_path` from `~/.claude/skills/publish-vault/config.json`
- If neither exists, refuse to run and direct the user to `INSTALL.md` to bootstrap their personal config. Do not write anywhere without an explicit, configured vault path.
- **Refuse to run if `default_vault_path` starts with `TODO:`** — that's the placeholder shipped in `config.template.json`. The user must replace it with an absolute path before the skill can do anything.

If `use_project_subfolder` is true (the default), the actual write target is `<resolved_vault_path>/<cwd-basename>/` — the project subfolder is created on first run. All subsequent operations (manifest path, derived notes, index notes) live inside that subfolder. The cwd basename is sanitized: spaces preserved, but path separators and shell-unsafe chars are stripped.

Verify the resolved vault directory exists (or create it with confirmation, including the project subfolder if applicable). Refuse to write to a path that's inside a git repo's working tree without explicit `--allow-in-repo` flag — the vault is supposed to be machine-local, not committed.

### 2. Load Manifest

Read `<vault>/.publish-vault-manifest.json` if it exists:

```json
{
  "version": 2,
  "prompt_version": 2,
  "vault_layout": "nested",
  "project_root": "/path/to/your/project",
  "sources": {
    "<absolute-source-path>": {
      "hash": "sha256:...",
      "vault_note": "03 — Modules/Features/<Feature> Feature (AGENTS).md",
      "source_type": "agents-md",
      "derived_at": "<iso-timestamp>"
    }
  }
}
```

`vault_note` is the relative path from the vault root (project subfolder), including any nested folders.

If `prompt_version` in the manifest differs from the config's `prompt_version`, treat every source as needing re-derivation.

If `vault_layout` in the manifest differs from the config (e.g., upgrading from `"flat"` to `"nested"`), perform a one-time reorganization: move existing notes to their new folder placement before deriving any new content. Wikilinks resolve by title regardless of folder, so they survive moves.

If `project_root` differs from cwd, ask before proceeding — the vault might be tracking a different project.

### 3. Walk and Classify

Walk cwd applying include/exclude globs. For each matched file:
- Compute SHA-256 of file content
- Classify into a source type (table above)
- Decide on a vault note title (see "Note Naming")
- Decide on a target folder (see "Folder placement rules")

### 4. Derive Notes

For each source file whose hash differs from the manifest entry (or has no entry):

**Invoke an LLM derivation per the source type's prompt.** The skill is the orchestrator; the actual prompts are inline below — adapt as needed. Always preserve technical accuracy; never invent details that aren't in the source.

Each derived note must start with this YAML frontmatter:
```
---
source: <relative-path-from-cwd>
type: <source-type>
---
```

Then a `# <Note Title>` H1, then the body.

#### `claude-md`
> Read this `CLAUDE.md`. Produce an Obsidian note titled `<Project> Project Orientation` that reframes the contents as a personal navigation entry-point: what the project is, the high-level architecture, where to find things. Aim for 200–400 words. Preserve all command examples verbatim. End with a "See also" section listing related notes (you'll get back-linked automatically).

#### `agents-md`
> Read this `AGENTS.md` for `<feature/module name>`. Produce an Obsidian note titled `<Feature Name> (AGENTS)` that condenses to: business purpose (1–2 sentences), key types (table), cross-feature dependencies, important conventions specific to this module. Drop the standard preamble. Aim for 150–300 words. Preserve type names and module paths verbatim.

#### `standard`
> Read this standards doc. Produce an Obsidian note titled `<Document Title>` that expands the contents for personal-knowledge consumption: spell out acronyms on first use, add motivating context where the source assumes prior knowledge, surface the *rationale* for rules (not just the rules). Preserve code examples verbatim. Aim for the same length or slightly longer than the source.

#### `skill`
> Read this `SKILL.md`. Produce an Obsidian note titled `<Skill Name> (Skill)` framed as: what it does (1 sentence), when to invoke it (bullet list), what it produces, key parameters. Aim for 100–200 words. This is a navigation note, not a re-statement of the skill — point at the source for the full procedure.

#### `template`
> Read this template. Produce an Obsidian note titled `<Template Name> (Template)` that documents: what kind of artifact it produces, the major sections/slots, when to use it. Aim for 100–200 words. Don't duplicate the template body — reference it.

#### `project-doc`
> Read this project doc. Produce an Obsidian note titled `<Document Title>` that reframes for personal-knowledge consumption: surface key concepts, expand abbreviations, link to related notes by mentioning concept names that other notes will pick up. Preserve any commands, code, or links verbatim. Aim for similar length to source unless the source is verbose.

### 5. Wikilink Pass

After all notes are derived, do a single pass over every derived note in the vault:

1. Build a map of `{ source filename → vault note title }` and `{ source path → vault note title }` from the manifest. Titles are folder-independent — wikilinks resolve by title regardless of folder location.
2. For each note, scan the body for:
   - Bare filename references (e.g., `architecture.md`, `OrderPresenter.kt`)
   - Path references (e.g., `.agents/standards/architecture.md`, `features/order/AGENTS.md`)
   - Module paths in backticks (e.g., `core:network`)
3. Where a referenced item maps to a vault note, rewrite it as `[[Note Title]]`
4. Leave unmatched references alone.
5. **Skip the `**Source**:` line** — it must keep the original backticked path, not become a self-wikilink.
6. **Skip self-references** — if a replacement would link a note to itself, drop the brackets and leave the title as plain text.
7. **Skip fenced code blocks** — never modify content inside ```` ``` ```` fences.

Be conservative — don't auto-link generic terms (`README`, `architecture`) unless the exact path matches.

### 6. Generate Index Notes

For each source type with at least one note, generate or update an index note:

| Index Note | Aggregates | Folder |
|------------|------------|--------|
| `Project Orientation Index.md` | `claude-md` notes | `01 — Orientation/` |
| `Project Docs Index.md` | `project-doc` and `other-md` notes | `01 — Orientation/` |
| `Standards Index.md` | All `standard` notes | `02 — Standards/` |
| `AGENTS Index.md` | All `agents-md` notes | `03 — Modules/` |
| `Skills Index.md` | All `skill` notes | `04 — Skills & Tooling/` |
| `Templates Index.md` | All `template` notes | `04 — Skills & Tooling/` |

Each index is a flat bulleted list of `[[Note Title]]` entries with a one-line description harvested from the note's first paragraph or **TL;DR** / **Purpose** / **What it is** field. Re-derive whenever any constituent note changes.

Also generate a top-level `<Project Name> Vault.md` in `00 — Vault Home/` that links the indexes and orientation notes — entry point for the project in the vault. Include a "Folder taxonomy" table so a user opening the vault cold can orient themselves.

### 7. Detect Orphans

Compare manifest entries against the current walk:
- **Orphan**: a manifest entry whose source path no longer exists in cwd → the source file was deleted
- **Path drift**: a manifest entry whose source path exists but content hash unchanged at a different path → source moved

For each orphan, **flag it in the summary, do not auto-delete**. Ask the user whether to:
- Remove the vault note
- Keep it (mark as "archived" by prepending `(Archived) ` to title)
- Skip for now

Destructive operations on a personal knowledge vault need explicit confirmation.

### 8. Update Manifest and Summarize

After all writes, update `<vault>/.publish-vault-manifest.json`:
- New/updated entries for derived notes (with full relative paths in `vault_note`)
- Remove entries for orphans the user confirmed to delete
- Bump `derived_at` timestamps on changed entries
- Record the `vault_layout` used so future runs can detect layout changes

Print a summary:
- N files scanned, M derived (new), K re-derived (changed), L unchanged
- Index notes touched
- Orphans found and how each was resolved
- Path to the vault and the top-level entry note

## Note Naming

- Use **Title Case**, filename only (no folder prefix in the title). Folder placement is determined separately.
- Strip filename extensions, replace `_` and `-` with spaces
- For `AGENTS.md`, prepend the parent directory name with a category suffix:
  - `core/<name>/AGENTS.md` → `<TitleCase Name> Core (AGENTS).md`
  - `features/<name>/AGENTS.md` → `<TitleCase Name> Feature (AGENTS).md`
  - `testing/<name>/AGENTS.md` → `<TitleCase Name> Testing (AGENTS).md`
  - other paths (e.g. `composeApp/AGENTS.md`) → use the parent directory name as the title (e.g. `ComposeApp (AGENTS).md`)
- For `SKILL.md`, prepend the parent directory: `.agents/skills/grill-me/SKILL.md` → `Grill Me (Skill).md`
- For `CLAUDE.md` at repo root: `<Project Name> Project Orientation.md` (project name from cwd basename)
- For `README.md` at repo root: `<Project Name> README.md`
- Disambiguate collisions by appending the parent path: `<Feature> Feature (AGENTS) — features-<name>.md`

Wikilinks reference titles only (e.g. `[[<Feature> Feature (AGENTS)]]`), never folder paths. This makes the vault's folder structure refactor-safe.

## Idempotency

The manifest's hash + prompt_version is the source of truth for whether a note needs re-derivation. On re-run:
- Source hash unchanged + prompt_version unchanged → skip (no LLM call, no write)
- Source hash changed → re-derive, update note, update manifest entry
- New source file → derive, write note, add manifest entry
- Source file gone → orphan flow (step 7)
- `vault_layout` changed in config → one-time reorg pass before any derivations

`--dry-run` performs all the walks, classifications, hash comparisons, and orphan detection — but skips LLM derivation and file writes. Reports what *would* change.

## Re-running

This skill is designed to be re-run manually after docs change in the source project. Typical cadence: after a sprint, after a refactor, when onboarding a teammate. Not on every commit.

If a derivation prompt in this skill is improved, bump `prompt_version` in config — that forces full re-derivation on the next run regardless of source hashes.

If `vault_layout` is changed (e.g., `"flat"` → `"nested"`), the next run reorganizes existing notes into the new layout before deriving any new content. Wikilinks survive moves because they resolve by title.

## Key Rules

- **Personal-machine only** — vault paths are local; never commit a configured vault path into a project repo. Refuse to write inside a git working tree without `--allow-in-repo`.
- **Project-agnostic** — no dependency on any specific project's skills (e.g., `/summarize`); discovery is glob-based; folder taxonomy uses generic names that work for any software project.
- **Derived, not copied** — every note is LLM-transformed per source type, not a literal mirror. The vault adds value through reframing + wikilinks + folder taxonomy, not duplication.
- **Idempotent on no-change** — re-running on an unchanged project produces no LLM calls and no writes.
- **Conservative wikilinking** — only link references that match a known source path or filename; don't infer.
- **Folder-refactor-safe** — wikilinks reference titles, not paths; reorganizing folders never breaks links.
- **Confirm before destruction** — orphans get flagged, never auto-deleted.
- **Preserve technical accuracy** — derivation may reword and condense, but never invents type names, paths, or behaviors not in the source.

## Power-user extensions

The personal version of this skill at `~/.claude/skills/publish-vault/SKILL.md` may include additional features (Maps of Content / MOC generation, vault-wide MOC linkage, custom themed indexes) that are not part of this shared template. Those features depend on individual Obsidian vault organization choices that don't generalize across developers, so they live only in personal copies. If you want them, see the personal-version source or ask whoever maintains the upstream.

## No Verification Needed

This skill is read/write on documents only — no code changes. The vault is a personal artifact, not a build output. Verify by opening the vault in Obsidian and checking the graph view + folder explorer.
