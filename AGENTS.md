# SamplePlatter

Kotlin Multiplatform reference app. Shared Kotlin business logic with native UI per platform: Jetpack Compose UI on Android, native SwiftUI on iOS. iOS uses the Compose _runtime_ (via Molecule) for state management only — not Compose UI for rendering. All iOS views are standard SwiftUI.

## Tech Stack

| Library | Role |
|---------|------|
| Circuit | Navigation, presenter/UI wiring, screen registry |
| Metro | Dependency injection (Anvil-compatible, compile-time) |
| CenterPost | Business logic framework (coroutine-based interactors) |
| Ktor | HTTP networking |
| Kotest | Test framework (BehaviorSpec, property testing) |
| Konsist | Kotlin architecture test enforcement (38 test classes in `testing/architecture-check/`) |
| Harmonize | iOS/Swift architecture test enforcement |
| Compose Multiplatform | Android: Compose UI rendering. iOS: Compose runtime only (state via Molecule) — SwiftUI renders natively |
| Molecule | Bridges `@Composable` presenter functions to `StateFlow` for iOS (Compose runtime, not UI) |
| KMP-NativeCoroutines | Bridges Kotlin `StateFlow`/`Flow` to Swift `AsyncSequence` for SwiftUI observation |

## Module Structure

```
features/{name}/
  api/domain/          — models, abstract use cases (public contracts)
  api/navigation/      — Screen objects, TestTags (public, Circuit-aware)
  impl/domain/         — UseCaseImpl, Repository interfaces (private implementation)
  impl/data/           — RepositoryImpl, DataSource (interface + Impl), DTOs (@Serializable, *Dto suffix), mappers
  impl/presentation/   — Presenter, UiState, Events, Compose UI (private implementation)
  test/                — Fakes for testing

core/
  analytics/           — AnalyticsDispatcher + AnalyticsEvent (api/), LoggingAnalyticsDispatcher + TrackAnalyticsEvent interactor (impl/), fakes (test/)
  auth/                — AuthManager interface (api/) + InMemoryAuthManager (impl/)
  build-config/        — Compile-time market/env/buildType config, api/impl/test split (AppBuildConfig facade + `isDebug` extension in api/, BuildKonfig+validateAllMarkets in impl/, FakeAppBuildConfig in test/; variant selection delegated to build-logic/`BuildVariantResolver` — `-P` flags, AGP variant task names, or defaults)
  centerpost/          — CenterPostInteractor, CenterPostSubjectInteractor, CenterPostDispatchers
  circuit/             — TabScreen, ProtectedScreen, FlowScreen, Parcelize expect/actual, CircuitProviders
  logger/              — Logger/Severity/LogWriter typealiases over Kermit (api/), KermitLoggerInitializer (impl/), NoOpLoggerInitializer (test/). Raw `co.touchlab.kermit` imports are Konsist-confined to this module.
  metro/               — AppGraph interface (shared DI contract)
  network/             — HttpClientFactory (api/) + impl with baked-in plugins (api/impl split)
  persistence/         — DatabaseDriverFactory (api/) + AndroidSqliteDriver/NativeSqliteDriver actuals (impl/) + FakeDatabaseDriverFactory (test/). The single `AppDatabase` is aggregated in `composeApp`; feature-owned `.sq` schemas live in `features/{name}/impl/data/src/commonMain/sqldelight/`.
  presentation/        — Compose/SwiftUI-facing helpers: `rememberCenterPost`, `collectAsState`, `rememberFlag`/`rememberConfig`, and the Android WebView primitive (`androidMain`; iOS has its own SwiftUI `WebView.swift`)
  remote-config/       — FeatureFlag/RemoteConfig contracts + RemoteConfigProvider (api/), Harness-backed provider (impl/), FakeRemoteConfigProvider (test/)
  strings/             — Android-only `R.string` resources populated by `pullTranslations` (Phrase). iOS reads its own `iosApp/iosApp/Resources/{locale}.lproj/` files.
  theme/               — SamplePlatterTheme, colors, typography, dimens, AdaptiveLayout. Kotlin sources are `androidMain`-only (Compose UI); iOS uses `iosApp/iosApp/Theme/SamplePlatterTheme.swift`. Only `composeResources/font/` lives in `commonMain`.
  test-fixtures/       — TestCenterPostDispatchers, KotestProjectConfig, StateRobot base
```

testing/architecture-check/   — Konsist architecture rules (38 test classes, host JVM)
testing/navint-tests/         — Navigation + integration tests (real presenters, fake data, real Circuit)
testing/e2e-tests/            — End-to-end journey tests (real everything, UI Automator)
testing/benchmarks/           — Android Macrobenchmarks (startup/scroll, `benchmark` build type)

Features: debug-menu, home, login, more, nutrition, order, profile, recents, rewards, scan

`debug-menu` is compiled into every variant and filtered at runtime via `AppBuildConfig.isDebug` (see `MoreTabExtension.isDebugOnly`) rather than being stripped from the module graph.

## Architecture Rules

> Full details: [`.agents/standards/architecture.md`](.agents/standards/architecture.md)

```
api ← impl/domain ← impl/data
api ← impl/presentation
```

- Presenters access domain ONLY through CenterPost interactors — never repositories or impl classes
- Presentation NEVER imports from impl/data or impl/domain
- Cross-feature imports: ONLY through other feature's `api/` modules
- Core modules NEVER import from features

## Naming Conventions

> Full details: [`.agents/standards/naming-conventions.md`](.agents/standards/naming-conventions.md)

| Type | Pattern | Location | Annotations |
|------|---------|----------|-------------|
| Screen | `{Feature}Screen` | api/navigation | `@Parcelize`, data object |
| Presenter | `{Feature}Presenter` | impl/presentation | `@CircuitInject`, `@Inject`, `@Composable` |
| UiState | `{Feature}UiState` | impl/presentation | data class : `CircuitUiState`, must have `eventSink` |
| Event | `{Feature}Event` | impl/presentation | sealed class (NOT interface — iOS interop) |
| Use case (abstract) | `Get{Feature}Content` | api/domain | extends `CenterPostSubjectInteractor` |
| Use case (impl) | `Get{Feature}ContentImpl` | impl/domain | `@ContributesBinding` |
| Repository (interface) | `{Feature}Repository` | impl/domain | — |
| Repository (impl) | `{Feature}RepositoryImpl` | impl/data | `@ContributesBinding` |
| DataSource (interface) | `{Feature}RemoteDataSource` | impl/data/remote/ | — |
| DataSource (impl) | `{Feature}RemoteDataSourceImpl` | impl/data/remote/ | `@ContributesBinding` |
| DTO | `{Name}Dto` | impl/data/remote/ | `@Serializable` data class |
| Fake | `Fake{Name}` | test/ | extends abstract class |
| TestTags | `{Feature}TestTags` | api/navigation | object with `const val` tags |

## DI Rules

> Full details: [`.agents/standards/dependency-injection.md`](.agents/standards/dependency-injection.md)

- `@ContributesBinding(AppScope::class)` on ALL Impl classes (use cases + repositories)
- `@CircuitInject({Screen}::class, AppScope::class)` + `@Inject` on presenters and Ui functions
- Presenters talk to domain exclusively through CenterPost interactors ([details](.agents/standards/centerpost.md))
- Presenters must NOT depend on Repository interfaces — only interactors
- Presenters must NOT use raw `CoroutineScope`/`launch`/`async` — use `rememberCenterPost()`

## Forbidden Patterns

> Full details with rationale: [`.agents/standards/forbidden-patterns.md`](.agents/standards/forbidden-patterns.md)

- No wildcard imports | No `println` / `System.out` | No `Thread.sleep` / `runBlocking`
- No `!!` operator | No `lateinit var` in shared code
- No `ViewModel` / `AndroidViewModel` — use Circuit presenters
- No raw `CoroutineScope` / `launch` / `async` — use CenterPost
- No hardcoded `Dispatchers.*` — use `CenterPostDispatchers`
- No `mockk` / `Mockito` — use fakes | No `runTest` / `UnconfinedTestDispatcher`

## Test Conventions

> Full details: [`.agents/standards/testing.md`](.agents/standards/testing.md)

- All tests: Kotest `BehaviorSpec` (Given/When/Then) — no other spec styles
- Every `UseCaseImpl`, `RepositoryImpl`, `Presenter` must have a test
- Every `Ui.kt` must have `UiTest` + `UiRobot` + `StateRobot` (Robot pattern)
- Fakes in `test/src/commonMain/` (NOT commonTest) — no mocks
- Tests must be meaningful: no change detectors, no magic numbers, no testing stdlib
- Presenter tests must test event handling, not just initial state
- UI tests must verify interactions, not just rendering

## Verification — ALWAYS RUN AFTER CODE CHANGES

> Full details: [`.agents/standards/verification.md`](.agents/standards/verification.md)

```bash
./gradlew detektMetadataCommonMain                            # 1.  Lint (Kotlin)
swiftlint --config .swiftlint.yml                             # 2.  Lint (Swift)
./gradlew testAndroidHostTest                                 # 3.  Unit tests (Kotlin, host)
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination '...'           # 4.  Unit tests (iOS, simulator — pure-logic)
./gradlew :testing:architecture-check:test                    # 5.  Architecture (Konsist)
swift test --package-path iosApp/ArchitectureCheck            # 6.  Architecture (Harmonize)
./gradlew connectedAndroidDeviceTest                          # 7.  UI component tests (Android, emulator)
xcodebuild test -scheme iOSApp -testPlan UIComponentTests -destination '...'    # 8.  UI component tests (iOS, simulator — ViewInspector)
./gradlew :testing:navint-tests:connectedAndroidDeviceTest    # 9.  Nav/int tests (Android, emulator)
xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination '...'         # 10. Nav/int tests (iOS, simulator)
./gradlew :testing:e2e-tests:connectedAndroidTest             # 11. E2E tests (Android, device/emulator)
xcodebuild test -scheme iOSApp -testPlan E2ETests -destination '...'            # 12. E2E tests (iOS, simulator)
./gradlew assemble                                            # 13. Full build
```

## Convention Plugins

| Plugin | Used By | Adds |
|--------|---------|------|
| `mockdonalds.kmp.library` | api modules, single-target core modules | Base KMP setup, Kotest |
| `mockdonalds.kmp.domain` | impl/domain modules | Metro DI (`@ContributesBinding`) + auto-adds `core:logger:api` to `commonMain` |
| `mockdonalds.kmp.data` | impl/data modules | Metro DI + kotlinx.serialization + auto-adds `core:logger:api` to `commonMain` |
| `mockdonalds.kmp.presentation` | impl/presentation modules | Compose Multiplatform + Circuit codegen + auto-adds `core:strings` to `androidMain` and `core:logger:api` to `commonMain` |
| `mockdonalds.phrase` | `core:strings` | Registers `pullTranslations` Gradle task pulling Phrase translations into Android XML + iOS `.lproj` files |

## Subagent Dispatch (use them)

This repo assumes any AI tool with a subagent/spawning capability (Claude Code `Agent`, Codex sub-tasks, Aider `architect`, Copilot agent task-forks, etc.). Use it — sequential `Read` + `grep` chains for broad surface mapping are the most common context drain.

**Dispatch when one of these fires:**

- **>3 modules touched** in a cross-layer change → one `Explore`-style agent for pre-flight surface mapping.
- **`verify diff` failures in ≥3 modules** → one `general-purpose` agent per affected module, in parallel.
- **Repository-wide reference hunt** (≥3 sequential greps for the same symbol) → one `general-purpose` agent.
- **Cross-tool infra investigation** (Xcode/Gradle/test plan/build config) → one `general-purpose` agent.

**Don't dispatch for:** trivial single-file work, or for any step in the file-edit-verify-iterate loop itself — that loop stays on the main agent.

Full trigger table, anti-patterns, and per-skill prompt templates: [`.agents/standards/ways-of-working.md`](.agents/standards/ways-of-working.md#when-to-spawn-subagents) and each affected skill's `SKILL.md` ("Pre-flight: Subagent dispatch" section).

## Agent Entry Points

This repo follows the vendor-neutral [AGENTS.md](https://agents.md) standard. `AGENTS.md` (root + per-module) and `.agents/` are the canonical, committed source of truth for every agent. Vendor-specific entry points are **deliberately not committed** — `.gitignore` excludes `CLAUDE.md`, `.claude/`, and `GEMINI.md` so the repo stays tool-agnostic.

Claude Code discovers skills at `.claude/skills/*/SKILL.md` only. It does **not** scan `.agents/skills/`. The symlink is therefore a **required, per-developer setup step** — without it, `/verify`, `/add-feature`, and the other 30 skills are invisible to Claude Code:

```bash
ln -s AGENTS.md CLAUDE.md
mkdir -p .claude && ln -s ../.agents/skills .claude/skills
```

**If you are an agent working in an environment where that symlink does not exist** (a fresh clone, CI, a cloud/web session), the skills are still fully usable — they are just not auto-loaded. Read `.agents/skills/{name}/SKILL.md` directly and follow it literally, including its "Pre-flight" gates and its **MANDATORY** Post-Change Verification section. Do not treat an unlisted skill as an unavailable one.

## Skills

Available automation in `.agents/skills/` (32 skills total). All scaffolding and modification skills accept optional context via `@file` spec reference or inline description — see `.agents/templates/` for spec templates.

### Verification & Quality

| Skill | Description |
|-------|-------------|
| `verify` | Unified pipeline — `diff` (default, changed modules), `full` (whole-project lint + unit + arch + one build per platform), `all` (every test level + every variant + full assemble) |
| `run-unit-tests` | Kotest unit tests + iOS Swift Testing |
| `run-ui-tests` | Android + iOS UI tests (requires device/simulator) |
| `run-arch-tests` | Konsist + Harmonize architecture tests |
| `run-all-tests` | Full test pipeline — lint + all 5 test levels on both platforms |
| `code-review` | Diff-based code review against default branch |
| `lint-branch` | Fast pre-commit lint — Detekt + SwiftLint on changed files (~5s) |
| `find-dead-code` | Surface unused declarations, orphaned TestTags/Screens/Fakes (optional module scope) |
| `summarize` | Project/feature/module overview with android/ios platform scope |
| `reverse-spec` | Reverse-engineer a spec from existing code — presumed AC, data flow, contracts |
| `benchmark` | Perfetto/Macrobenchmark (Android) + Instruments (iOS) benchmarking and tracing |
| `run-e2e-tests` | End-to-end journey tests (Android UI Automator + iOS XCUITest) |

### Test Generation

| Skill | Description |
|-------|-------------|
| `add-unit-tests` | Fill unit test gaps from branch diff |
| `add-ui-tests` | Fill UI test gaps from branch diff |
| `add-tests` | Combined unit + UI gap-filling |

### Scaffolding

| Skill | Description |
|-------|-------------|
| `add-feature` | Scaffold complete feature module (6 submodules + tests + AGENTS.md) |
| `add-screen` | Add screen to existing feature (9+ files) |
| `add-use-case` | Add interactor (abstract + impl + fake + test) |
| `add-repository` | Add repository (interface + impl + test + data sources + DTOs) |
| `add-core-module` | Scaffold core module with api/impl split |
| `add-api-endpoint` | Wire feature to backend API (data source + DTO + mapper + client config) |
| `add-analytics-events` | Add analytics event definitions + presenter/domain wiring |
| `add-feature-flag` | Add feature flag definition + observation + gating |
| `add-monitoring` | Add observability instrumentation (shell — core:monitoring planned) |
| `add-config-field` | Add compile-time field to `core:build-config` |
| `add-market` | Add a new market (build-config combo files, AGP flavor, iOS xcconfigs, store identity) |
| `validate-all-markets` | Enforce build-config schema/format rules across all market properties |

### Modification

| Skill | Description |
|-------|-------------|
| `update` | Modify existing code across all affected layers |
| `remove` | Clean teardown across layers with dependency analysis |
| `migrate` | Cross-cutting migration (library swap, pattern change, API version upgrade) |

### Spec Generation

| Skill | Description |
|-------|-------------|
| `ac-to-spec` | Convert acceptance criteria, Jira tickets, Gherkin, or PRDs into a filled-in spec template |
| `grill-me` | Interrogate a spec for unresolved markers, gaps, and unconfirmed presumptions before scaffolding |

### Spec Templates

Structured input for skills — copy, fill in, feed via `@file`. Located in `.agents/templates/`:

| Template | Purpose |
|----------|---------|
| `new-spec.md` | Describe something new to build |
| `change-spec.md` | Describe a modification to existing code |
| `migrate-spec.md` | Describe a migration from one approach to another |
| `remove-spec.md` | Describe what to tear down |

## Standards Reference

Detailed reference documents in `.agents/standards/`:

| Standard | Covers |
|----------|--------|
| [architecture.md](.agents/standards/architecture.md) | Layers, dependency flow, module structure, convention plugins, wiring |
| [naming-conventions.md](.agents/standards/naming-conventions.md) | All naming patterns with rationale, visibility rules |
| [dependency-injection.md](.agents/standards/dependency-injection.md) | Metro/DI patterns, presenter-domain contract |
| [testing.md](.agents/standards/testing.md) | Test levels overview, quality standards, fakes, infrastructure |
| [testing-unit.md](.agents/standards/testing-unit.md) | Unit tests: Kotest BehaviorSpec, presenter/use case/repo patterns |
| [testing-ui-component.md](.agents/standards/testing-ui-component.md) | UI component tests: Robot pattern (Android + iOS), TestTags |
| [testing-navint.md](.agents/standards/testing-navint.md) | Navigation/integration tests: Android navint + iOS navint |
| [testing-e2e.md](.agents/standards/testing-e2e.md) | E2E tests: journeys, AppRobot, Macrobenchmark |
| [testing-architecture.md](.agents/standards/testing-architecture.md) | Architecture tests: Konsist + Harmonize rules and categories |
| [testing-benchmarks.md](.agents/standards/testing-benchmarks.md) | Benchmarks: Android Macrobenchmark + iOS Instruments, `benchmark` build type |
| [centerpost.md](.agents/standards/centerpost.md) | Interactor patterns, presenter integration, error handling |
| [forbidden-patterns.md](.agents/standards/forbidden-patterns.md) | Every banned pattern with WHY and alternative |
| [verification.md](.agents/standards/verification.md) | Pipeline steps, scoped verification, failure interpretation |
| [ios-interop.md](.agents/standards/ios-interop.md) | Bridge patterns, sealed class vs interface, SwiftUI conventions |
| [ways-of-working.md](.agents/standards/ways-of-working.md) | Skill usage, code review, contribution workflow |
| [code-style.md](.agents/standards/code-style.md) | Detekt/ktlint/SwiftLint rules, Compose style |
| [design-system.md](.agents/standards/design-system.md) | Theme, colors, tokens, adaptive layout, landscape |
| [convention-plugins.md](.agents/standards/convention-plugins.md) | Plugin hierarchy, what each plugin provides, auto-wiring |
| [feature-scaffolding.md](.agents/standards/feature-scaffolding.md) | Step-by-step guide to add a new feature, checklist |
| [build-config.md](.agents/standards/build-config.md) | Compile-time market/env config (`core:build-config`): BuildKonfig schema, `AppBuildConfig` facade, Harness boundary, add-a-field workflow |
| [markets.md](.agents/standards/markets.md) | Cross-cutting market concept: the 5 markets, how each surfaces across Android (applicationId), iOS (bundle ID, xcconfig), CI axis, future localization/analytics/store listings |

## Self-Updating Documentation

> Full details: [`.agents/standards/ways-of-working.md`](.agents/standards/ways-of-working.md#self-updating-documentation)

Agent files must stay in sync with the codebase. When your work changes conventions, patterns, or structure, update the relevant AGENTS.md, standards, or skill files as part of the same change. Documentation that drifts from reality actively misleads — treat updates as part of the definition of done.

## Module-Specific Context

Per-module AGENTS.md files load via JIT context when you access files in those directories:

- `features/{name}/AGENTS.md` — feature business context, key types, cross-feature deps
- `core/{module}/AGENTS.md` — module purpose, public API, usage patterns
- `testing/architecture-check/AGENTS.md` — architecture test categories and how to add rules
- `testing/navint-tests/AGENTS.md` — navigation + integration test infrastructure, NavIntAppGraph
- `testing/e2e-tests/AGENTS.md` — end-to-end journey tests, AppRobot, benchmarks
- `iosApp/AGENTS.md` — Swift bridge patterns, Harmonize tests
- `build-logic/AGENTS.md` — convention plugin details
- `composeApp/AGENTS.md` — app entry points, navigation wiring, deep links
- `.agents/AGENTS.md` — how to use and create skills
