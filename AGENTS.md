# MockDonalds

Kotlin Multiplatform reference app. Shared Kotlin business logic with native UI per platform: Jetpack Compose UI on Android, native SwiftUI on iOS. iOS uses the Compose _runtime_ (via Molecule) for state management only — not Compose UI for rendering. All iOS views are standard SwiftUI.

## Tech Stack

| Library | Role |
|---------|------|
| Circuit | Navigation, presenter/UI wiring, screen registry |
| Metro | Dependency injection (Anvil-compatible, compile-time) |
| CenterPost | Business logic framework (coroutine-based interactors) |
| Ktor | HTTP networking |
| Kotest | Test framework (BehaviorSpec, property testing) |
| Konsist | Kotlin architecture test enforcement (22 test classes in `testing/architecture-check/`) |
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
  auth/                — AuthManager interface (api/) + InMemoryAuthManager (impl/)
  build-config/        — Compile-time market/env config (BuildKonfig-backed AppBuildConfig facade; -Pmarket/-Penv)
  centerpost/          — CenterPostInteractor, CenterPostSubjectInteractor, CenterPostDispatchers
  circuit/             — TabScreen, ProtectedScreen, FlowScreen, Parcelize expect/actual, CircuitProviders
  metro/               — AppGraph interface (shared DI contract)
  network/             — HttpClientFactory (api/) + impl with baked-in plugins (api/impl split)
  theme/               — MockDonaldsTheme, colors, typography, dimens, AdaptiveLayout
  test-fixtures/       — TestCenterPostDispatchers, KotestProjectConfig, StateRobot base
```

testing/navint-tests/         — Navigation + integration tests (real presenters, fake data, real Circuit)
testing/e2e-tests/            — End-to-end journey tests + benchmarks (real everything, UI Automator)

Features: home, login, more, order, profile, rewards, scan

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
| `mockdonalds.kmp.library` | api modules | Base KMP setup, Kotest |
| `mockdonalds.kmp.domain` | impl/domain modules | Metro DI (`@ContributesBinding`) |
| `mockdonalds.kmp.data` | impl/data modules | Metro DI + kotlinx.serialization |
| `mockdonalds.kmp.presentation` | impl/presentation modules | Compose Multiplatform + Circuit codegen |

## Skills

Available automation in `.agents/skills/`:

| Skill | Description |
|-------|-------------|
| `verify` | Full verification pipeline (build + lint + all tests) |
| `verify-smart` | Diff-aware verification — scopes checks to changed modules |
| `run-unit-tests` | Run Kotest unit test suite |
| `run-ui-tests` | Run Android UI tests for a specific feature |
| `run-arch-tests` | Run Konsist + Harmonize architecture tests |
| `run-all-tests` | Lint + unit tests + architecture tests |
| `code-review` | Diff-based code review against default branch |
| `add-unit-tests` | Identify and fill unit test gaps from branch diff |
| `add-ui-tests` | Identify and fill UI test gaps from branch diff |
| `add-tests` | Combined unit + UI test gap-filling |
| `add-feature` | Scaffold a complete new feature (6 modules + tests + AGENTS.md) |
| `add-screen` | Add a new screen to an existing feature (9+ files) |
| `add-use-case` | Add interactor (abstract + impl + fake + test) |
| `add-repository` | Add repository (interface + impl + test) |
| `add-config-field` | Add a compile-time field to `core:build-config` (Defaults + every combo + facade + smoke test) |
| `validate-all-markets` | Gradle task on `:core:build-config`: parse every `markets/*.properties` against `Defaults.properties`, enforce schema/format rules. Pre-flight gate for `verify` and `verify-ci`. |
| `lint-branch` | Fast pre-commit lint: Detekt on commonMain + SwiftLint on changed `.swift` files only. ~5s. For thorough diff-scoped checks, use `verify-smart`. |
| `find-dead-code` | Surface unused Kotlin declarations (full Detekt across all source sets) + dead TestTags / Screens / Fakes via targeted greps. Reports only; never deletes. |

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
