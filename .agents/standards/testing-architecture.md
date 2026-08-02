# Architecture Testing Standards

Architecture tests enforce project conventions via static analysis. They prevent structural drift — wrong dependency directions, missing annotations, naming violations, forbidden patterns — without running the app. Two complementary tools cover Kotlin and Swift.

> Shared conventions (test stack, quality standards, fakes, infrastructure) are in [testing.md](testing.md).

## Scope

| What's tested | What's real | What's faked |
|---------------|-------------|--------------|
| Source code structure, imports, annotations, naming | Static analysis of all project source files | Nothing — no runtime |

## Run Commands

```bash
# Kotlin architecture tests (Konsist)
./gradlew :testing:architecture-check:test

# iOS architecture tests (Harmonize)
swift test --package-path iosApp/ArchitectureCheck
```

No emulator or simulator required. These are fast (~10s each) and should always run.

## Konsist (Kotlin)

38 architecture test classes in `testing/architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/`. All use Kotest BehaviorSpec and scan the project with `Konsist.scopeFromProject()`.

### Source-set scoping — read this before writing a rule

**A rule scoped to `commonMain` alone checks almost nothing.** Every `*Ui.kt` in this project lives in `androidMain`, and the Molecule/Circuit bridge lives in `iosMain`. A `commonMain`-only filter silently exempts the single largest body of code in the repo — a UI file could use `GlobalScope`, hardcode `Dispatchers.IO`, or import from `impl/data`, and the rule would pass.

Use the shared predicate from `konsist/ScopePredicates.kt`:

```kotlin
.filter { isProductionSourcePath(it.path) }   // commonMain + androidMain + iosMain
```

Two rules legitimately stay `commonMain`-only, and both say so at the call site:

- `ForbiddenPatternsTest` → "no Android platform imports in commonMain" — the rule *is* about that source set.
- Rules governing module types that are `commonMain`-only by construction (`impl/domain`, `impl/data`, feature `api/`). Widening them is harmless but adds no coverage.

Related trap: **never compare a feature directory name against a package segment directly.** Directory names may be hyphenated (`debug-menu`); package segments cannot (`debugmenu`). A raw comparison silently disables the rule for every hyphenated feature. Use `featurePackageSegment(path)`.

Third trap: **do not identify core impl types by an `.impl.` substring in the import.** Only `analytics`, `logger`, and `remote-config` namespace impl under `.impl`; `auth`, `build-config`, `network`, and `persistence` share their api module's package. Derive the impl surface from the module path instead — see `isCoreImplPath`.

### Test Categories

| Category | Tests | What They Enforce |
|----------|-------|-------------------|
| architecture/ | `LayerDependencyTest` | Unidirectional dependency flow: api <- domain <- data, api <- presentation. Cross-feature imports only via api. Core never imports features. Network module (`core.network`) imports restricted to `impl/data` modules only. |
| architecture/ | `CircularDependencyTest` | No circular api imports between feature modules. |
| architecture/ | `ForbiddenPatternsTest` | No ViewModels, no raw CoroutineScope/launch/async/Dispatchers, no Android platform imports in commonMain. **Compose runtime but not Compose UI in commonMain/iosMain** — anything in commonMain compiles for all three iOS targets, so a Compose UI import there drags the material3/foundation/ui stack into the iOS framework for code SwiftUI never reaches. |
| circuit/ | `CircuitConventionsTest` | Events are sealed class (not interface). Screens in api with @Parcelize. TabScreens have tag. ProtectedScreens in api/navigation. |
| circuit/ | `NamingConventionsTest` | Screens end with Screen, events with Event, presenters with Presenter, UiState with UiState, repositories with Repository/RepositoryImpl. |
| core/ | `CodeHygieneTest` | No wildcard imports, no println/System.out, no Thread.sleep/runBlocking, no !!, no lateinit var in shared code. |
| core/ | `DependencyInjectionTest` | Every Repository has @ContributesBinding impl. Every abstract use case has @ContributesBinding Impl. @CircuitInject presenters have @Inject. |
| core/ | `PackageConventionsTest` | Package segments match module path. Features use com.mockdonalds.app.features.*. Core uses com.mockdonalds.app.core.*. |
| core/ | `VisibilityConventionsTest` | @ContributesBinding classes are public. UiState not internal. |
| core/ | `DependencyGraphScopeTest` | @DependencyGraph only in consumer modules (composeApp, navint-tests). CircuitProviders only in core:circuit. AppGraph in core:metro. |
| core/ | `CoreMetroConventionsTest` | core:metro must not import from features or impl modules. |
| core/ | `CoreStringsConventionsTest` | core:strings is resources-only (no Kotlin source, androidMain only). No feature `build.gradle.kts` declares `:core:strings` — the presentation plugin auto-wires it on `androidMain`. |
| core/ | `BuildConfigSchemaParityTest` | `AppBuildConfig` ↔ `Defaults.properties` key parity (camelCase ↔ SCREAMING_SNAKE_CASE). Every market directory covers the same set of envs. Catches schema drift in PRs without invoking the full `validateAllMarkets` Gradle task. |
| core/ | `PersistenceConventionsTest` | `app.cash.sqldelight.*` imports restricted to `core:persistence` (api/impl/test), the `composeApp` aggregator, and feature `impl/data/`. Keeps SQLDelight runtime out of presentation/domain and prevents non-data modules from reaching past repositories into raw queries. |
| core/ | `AgentDocumentationTest` | Every feature/core module has AGENTS.md. Skills have SKILL.md. Every standards file exists. |
| core/ | `AgentDocumentationDriftTest` | Every feature, core module, skill, and standard on disk is named in `AGENTS.md` / `.agents/AGENTS.md` / `README.md`. No skill named in the docs is missing from disk. The Konsist class count quoted in AGENTS.md matches the suite. Agents route off these files — an unlisted skill is an uninvokable one. |
| circuit/ | `UiCompositionConventionsTest` | Every `*Ui.kt` exposes a composable named after the file carrying `@CircuitInject` (absence must fail, not narrow `UiTestConventionsTest`'s filter). Helper composables are private/internal — `composeApp` consumes presentation via `api(project(...))`, so public helpers leak into app-wide API surface. |
| circuit/ | `PlatformParityTest` | Every Kotlin `@CircuitInject({Screen}::class)` has a matching Swift `@CircuitInject({Screen}.self)` view, and vice versa. Without it a feature can pass the whole suite on both platforms and still render `Text("No UI for screen: …")` on iOS. |
| layers/ | `ApiLayerTest` | Api data classes are immutable. @Serializable only in api/data/network. No MutableStateFlow in public APIs. |
| layers/ | `DataLayerTest` | Repository interfaces in domain. RepositoryImpl in data with @ContributesBinding. Repository functions return Flow. DataSource classes in correct directories (remote/ or local/). DTOs must be @Serializable data classes in remote/ package. |
| layers/ | `DomainLayerTest` | Abstract use cases in api. Impl classes in domain with @ContributesBinding. |
| layers/ | `PresentationLayerTest` | Presenters have @CircuitInject. One public function per presenter file. No direct repository dependencies. |
| testing/ | `TestDoubleConventionsTest` | Fakes in test/commonMain. Every use case has a Fake. No mockk. |
| testing/ | `TestModuleDITest` | Fakes have @ContributesBinding + @Inject. No @ContributesBinding in commonTest. |
| testing/ | `TestFileNamingTest` | All specs extend BehaviorSpec. No runBlocking/runTest/UnconfinedTestDispatcher. |
| testing/ | `TestModuleCoverageTest` | Every Impl, Presenter, RepositoryImpl has a test file. Every TestTags referenced in e2e. |
| testing/ | `TestBoundaryTest` | UI tests don't use Navigator. navint-tests don't import feature robots or impl/domain/data. e2e-tests don't import fakes or impl modules. |
| testing/ | `UiTestConventionsTest` | Robot pattern structure, theme wrapping, landscape methods, TestTags in api. |

### Adding Konsist Rules

1. Create a new `BehaviorSpec` in the appropriate package under `testing/architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/`
2. Use `Konsist.scopeFromProject()` for project-wide checks
3. Use `resideInPath("..impl/domain..")` for module-scoped checks
4. Use `Konsist.scopeFromSourceSet("commonMain", "features..", "domain")` for source-set-scoped checks
5. Filter with `isProductionSourcePath(it.path)` to exclude test code from production rules — **not** `resideInPath("..commonMain..")`, which also excludes `androidMain` and `iosMain`. See "Source-set scoping" above.
6. If the rule compares a collected set against another (parity, coverage, inventory), assert both sides are non-empty first. An empty-vs-empty comparison passes vacuously and hides every real violation — that is how `LocalizationConventionsTest` came to pass against a `Resources/` directory that does not exist.

## Harmonize (iOS/Swift)

48 tests across 3 files in `iosApp/ArchitectureCheck/Tests/HarmonizeTests/`. Uses XCTest via SPM package.

### Test Files

| File | What It Enforces |
|------|-----------------|
| `ViewConventionsTest.swift` | SwiftUI views follow naming patterns, use correct modifiers, proper accessibility identifiers |
| `TestConventionsTest.swift` | Robot pattern (ViewTest/ViewRobot/StateRobot per View), encapsulation, landscape coverage, navint test conventions, E2E test conventions |
| `LocalizationConventionsTest.swift` | All `.strings` files live under `iosApp/iosApp/Resources/{locale}.lproj/` — the only path written by the Phrase `pullTranslations` task. |

### Adding Harmonize Rules

1. Add test methods to existing test files or create new `*Test.swift` in `iosApp/ArchitectureCheck/Tests/HarmonizeTests/`
2. Use `Harmonize.codebase()` to scan Swift source files
3. Filter with `.in(.targets(named: "iosApp"))` for production code or `.in(.targets(named: "iosAppTests"))` for test code
4. Assert conventions with descriptive failure messages

## When to Run

Architecture tests should **always** run — they're fast and catch structural issues regardless of what changed:
- Every PR: both Konsist and Harmonize
- After scaffolding a new feature or module
- After modifying build.gradle.kts, DI annotations, or navigation
- After adding/modifying tests (test convention enforcement)

## Failure Interpretation

### Konsist
- Reports: rule name + violating class/file path
- Message explains the specific convention violated (e.g., "Presenters must not depend on repositories directly")
- Fix the structural issue, not the test

### Harmonize
- Reports: Swift convention + violating struct/class
- Check view naming patterns, test module organization, or accessibility identifiers
- Fix the Swift source, not the Harmonize test
