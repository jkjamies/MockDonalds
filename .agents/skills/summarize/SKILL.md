---
name: summarize
description: Generate a structured overview of the project, a feature, or a core module. Supports android/ios scope for platform-specific focus. Use for onboarding, context-switching, or reviewing unfamiliar code.
---

# Summarize

Generate a structured briefing about the project, a feature, or a core module.

**Parameters**: target (optional — omit for whole project), platform scope (optional — `android` or `ios`)

**Usage examples**:
```
/summarize                    # whole project overview
/summarize order              # single feature deep-dive
/summarize core:network       # core module deep-dive
/summarize android            # project overview, Android focus
/summarize ios                # project overview, iOS focus
/summarize order android      # order feature, Android focus
/summarize order ios          # order feature, iOS focus
```

## Information Sources

Read these in order of priority:

| Source | What it provides |
|--------|-----------------|
| `CLAUDE.md` | Build commands, architecture overview, key versions |
| `.agents/AGENTS.md` | Skill categories, standards index |
| `.agents/standards/` | Architecture, naming, DI, testing, design system conventions |
| `features/{name}/AGENTS.md` | Feature business context, key types, dependencies |
| `core/{name}/AGENTS.md` | Core module purpose, public API, consumption patterns |
| Feature source files | Actual implementation details |
| `git log` (recent) | Recent activity and direction |

## Output Format

### Project Overview

```
# Project Summary: MockDonalds

## What This Is
[1-2 sentences: KMP app, platforms, purpose]

## Architecture at a Glance
[Module structure, key patterns, dependency flow diagram]

## Features
[Table: feature name, description, screen count, status]

## Core Modules
[Table: module name, purpose, key types]

## Tech Stack
[Key libraries and versions]

## Build & Run
[Essential commands to get started]

## Conventions That Matter Most
[Top 5-10 rules a new dev will hit first]

## Where to Start
[Recommended first files to read, reference feature to study]
```

### Feature Deep-Dive

```
# Feature Summary: {name}

## Business Context
[What it does, user workflow, why it exists]

## Module Structure
[6 submodules with file counts and key types]

## Data Flow
[Domain models → use cases → repository → data sources → API]

## Screens & Navigation
[Screen types, entry points, outgoing navigation, deep links]

## State Management
[UiState shape, events, presenter logic summary]

## Cross-Feature Dependencies
[What it imports, what imports it]

## Testing Coverage
[Test files, what's covered, gaps]

## Platform-Specific Notes
[Android: Compose UI details]
[iOS: SwiftUI bridge details]

## Recent Activity
[Last N commits touching this feature]
```

### Core Module Deep-Dive

```
# Core Module Summary: {name}

## Purpose
[What problem it solves, why it's a core module]

## Public API
[Key interfaces, classes, interactors — what consumers use]

## Implementation
[How it works internally]

## Consumers
[Which features/modules depend on it]

## Configuration
[Build config, DI setup, convention plugin integration]

## Testing
[How to test, fakes provided]
```

## Platform Scoping

When `android` or `ios` is specified, focus the summary on platform-relevant details:

### Android Focus
- Compose UI implementation details (`androidMain/`)
- Convention plugins and Gradle build config
- Android-specific test setup (device tests, Compose test rules)
- `composeApp` module wiring
- Detekt lint rules

### iOS Focus
- SwiftUI views and navigation (`iosApp/`)
- KMP-NativeCoroutines bridge patterns
- iOS-specific test setup (XCTest, test plans, simulator)
- Xcode project structure
- SwiftLint rules
- Harmonize architecture tests

## Key Rules

- **Read, don't guess** — always read AGENTS.md and source files, never summarize from memory
- **Be specific** — include file paths, type names, line counts
- **Note gaps** — if something is missing or incomplete, say so
- **Recency matters** — check git log for recent changes that AGENTS.md might not reflect
- **Tailor depth to scope** — project overview is broad, feature deep-dive is detailed

## No Verification Needed

This skill is read-only — it produces a summary, not code changes. No verification step required.
