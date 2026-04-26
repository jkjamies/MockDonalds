# local-setup

**Personal-install skill templates.** Not active skills, not project code.

## Purpose

Houses opt-in Claude Code skill templates that each developer installs onto their personal machine. Each template ships a sanitized `SKILL.md`, a `config.template.json` with `TODO:` placeholders for personal values, and an `INSTALL.md` walking through the copy + per-developer config bootstrap.

## Why this folder exists

- The skills here write to paths **outside** the repo (e.g., personal Obsidian vaults). They must not run from a checked-in skill location, so they cannot live in `.agents/skills/`.
- Personal config (vault paths, machine-specific paths) belongs to the developer, not the project. Keeping these skills opt-in lets each developer customize without polluting the shared codebase.
- At team scale, a shared template baseline cuts onboarding time. Without it, every new dev would reinvent the same skill from scratch.

## Available templates

| Template | Purpose | Install steps |
|----------|---------|---------------|
| [`publish-vault/`](publish-vault/SKILL.md) | Publish project docs as derived Obsidian notes (nested taxonomy, wikilinks, indexes); idempotent diff updates on re-run | [`publish-vault/INSTALL.md`](publish-vault/INSTALL.md) |

## Per-developer TODO

For each template you want to use:

1. Read its `INSTALL.md`
2. Copy `SKILL.md` to `~/.claude/skills/<name>/`
3. Copy `config.template.json` to `~/.claude/skills/<name>/config.json` and **fill in the `TODO:` placeholders** — never commit your filled-in config back to the repo

The `config.template.json` shipped with each template has `TODO:`-prefixed values for any field that needs a personal answer (vault path, etc.). The active SKILL.md refuses to run while those `TODO:` markers remain. This makes the per-developer setup step impossible to skip silently.

## Adding a new template

1. Create `local-setup/<skill-name>/SKILL.md` — sanitize any personal paths or names from your working copy. Use `<placeholder>` syntax for fields the user must fill in.
2. Create `local-setup/<skill-name>/config.template.json` — minimal scaffold with `TODO:`-prefixed values for any field requiring a personal answer. The active SKILL.md should refuse to run while `TODO:` markers remain.
3. Create `local-setup/<skill-name>/INSTALL.md` — copy commands (both SKILL.md and config.template.json), TODO checklist, verification step.
4. Update this AGENTS.md's "Available templates" table.

## Auto-discovery

Claude Code's skill auto-discovery scans `.agents/skills/*/SKILL.md` and `.claude/skills/*/SKILL.md`. This folder (`.agents/local-setup/`) is outside both — Claude Code will not load these as active skills. Other agents searching the repo will still see them; the bold "TEMPLATE — NOT AN ACTIVE SKILL" warning at the top of each `SKILL.md` is the safeguard.

If you ever need to run one of these against the repo without installing it personally (rare — usually a smell), you can pass the SKILL.md path explicitly to a one-shot invocation. But the standard path is install-then-invoke.

## What does NOT live here

- Personal `config.json` files (those live at `~/.claude/skills/<name>/config.json`, never in this repo)
- Active project skills (those live at `.agents/skills/`)
- Vault paths, personal Obsidian organization details, individual developer preferences
