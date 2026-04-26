# Install: publish-vault

Personal-install template for the `/publish-vault` Claude Code skill. The skill is opt-in — it does not run on your machine until you do the steps below. ~5 minutes.

## What this skill does

Walks the current project's docs (`AGENTS.md`, `CLAUDE.md`, `.agents/`, `README.md`, etc.) and publishes them as a navigable Obsidian knowledge base — derived notes, wikilinks, indexes. Idempotent: re-runs only re-derive sources whose content changed (diff updates). Read `SKILL.md` next to this file for the full mechanic.

## TODO checklist (per developer)

1. Have an Obsidian vault somewhere on disk. If you don't, create one — Obsidian stores vaults as plain folders, no app server required.
2. Pick a parent folder inside that vault to hold per-project subfolders. Common choice: `<vault>/Engineering/Projects/` or `<vault>/Work/Projects/`.
3. Copy the skill onto your machine:
   ```bash
   mkdir -p ~/.claude/skills/publish-vault
   cp .agents/local-setup/publish-vault/SKILL.md ~/.claude/skills/publish-vault/SKILL.md
   cp .agents/local-setup/publish-vault/config.template.json ~/.claude/skills/publish-vault/config.json
   ```
4. **TODO: edit `~/.claude/skills/publish-vault/config.json`**:
   - Replace `default_vault_path` with the absolute path to your vault parent folder (currently starts with `TODO:` — the skill refuses to run until you fix this).
   - Delete the `_TODO` key.
   - Leave the rest at defaults until you have a reason to change them.
5. Verify: from any project root, run `/publish-vault --dry-run`. The skill should report what it WOULD generate without writing anything.
6. First real run: `/publish-vault`. Expect ~1 LLM derivation per source markdown file (parallelized). Subsequent runs are nearly free until docs change — only files whose SHA-256 changed get re-derived.

## What's in the config

| Key | Why it matters |
|-----|----------------|
| `default_vault_path` | Absolute path to the parent folder for per-project vault subfolders. Required. |
| `use_project_subfolder` | If `true`, the skill writes to `<default_vault_path>/<project-name>/` so multiple projects coexist. |
| `vault_layout` | `"nested"` (Confluence-style numbered folders) or `"flat"` (single-folder vault). |
| `include_globs` | Doc paths the skill walks. Defaults cover common conventions; rarely need editing. |
| `exclude_globs` | Build/dependency outputs to skip. Add to this if your project has unusual build directories. |
| `prompt_version` | Bump this to force full re-derivation on the next run (e.g., after upgrading SKILL.md). |

The personal version of `SKILL.md` (the one in your home directory) may include additional features not exposed in this template (Maps of Content, vault-wide MOC linkage, etc.). Those depend on individual Obsidian vault organization choices that don't generalize across developers, so they're not part of the shared template.

## Updating

When `~/.claude/skills/publish-vault/SKILL.md` is updated in the repo, re-copy:
```bash
cp .agents/local-setup/publish-vault/SKILL.md ~/.claude/skills/publish-vault/SKILL.md
```
Your `config.json` is unaffected — it lives outside the repo.

If a `prompt_version` bump appears in the new SKILL.md, also bump `prompt_version` in your `config.json` to force re-derivation of all notes on the next run. The skill will continue to work without the bump, but new prompt improvements won't reach already-derived notes until you bump.

If new config keys appear in `config.template.json`, diff against your `config.json` and merge in any you care about. Old keys still work — extras in your config are ignored.

## Uninstalling

```bash
rm -rf ~/.claude/skills/publish-vault
```
The vault folder itself is left intact — it's your data.

## Why isn't this an active project skill?

The skill writes to a path outside the repo (your personal Obsidian vault), so it must not run from a checked-in skill location. Treating it as a per-developer install also keeps personal vault paths out of git. See `../AGENTS.md` for the broader local-setup convention.
