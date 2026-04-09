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

22 architecture test classes in `testing/architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/`. All use Kotest BehaviorSpec and scan the project with `Konsist.scopeFromProject()`.

### Test Categories

| Category | Tests | What They Enforce |
|----------|-------|-------------------|
| architecture/ | `LayerDependencyTest` | Unidirectional dependency flow: api <- domain <- data, api <- presentation. Cross-feature imports only via api. Core never imports features. |
| architecture/ | `CircularDependencyTest` | No circular api imports between feature modules. |
| architecture/ | `ForbiddenPatternsTest` | No ViewModels, no raw CoroutineScope/launch/async/Dispatchers, no Android platform imports in commonMain. |
| circuit/ | `CircuitConventionsTest` | Events are sealed class (not interface). Screens in api with @Parcelize. TabScreens have tag. ProtectedScreens in api/navigation. |
| circuit/ | `NamingConventionsTest` | Screens end with Screen, events with Event, presenters with Presenter, UiState with UiState, repositories with Repository/RepositoryImpl. |
| core/ | `CodeHygieneTest` | No wildcard imports, no println/System.out, no Thread.sleep/runBlocking, no !!, no lateinit var in shared code. |
| core/ | `DependencyInjectionTest` | Every Repository has @ContributesBinding impl. Every abstract use case has @ContributesBinding Impl. @CircuitInject presenters have @Inject. |
| core/ | `PackageConventionsTest` | Package segments match module path. Features use com.mockdonalds.app.features.*. Core uses com.mockdonalds.app.core.*. |
| core/ | `VisibilityConventionsTest` | @ContributesBinding classes are public. UiState not internal. |
| core/ | `DependencyGraphScopeTest` | @DependencyGraph only in consumer modules (composeApp, navint-tests). CircuitProviders only in core:circuit. AppGraph in core:metro. |
| core/ | `CoreMetroConventionsTest` | core:metro must not import from features or impl modules. |
| core/ | `AgentDocumentationTest` | Every feature/core module has AGENTS.md. Skills have SKILL.md. |
| layers/ | `ApiLayerTest` | Api data classes are immutable. @Serializable only in api/data/network. No MutableStateFlow in public APIs. |
| layers/ | `DataLayerTest` | Repository interfaces in domain. RepositoryImpl in data with @ContributesBinding. Repository functions return Flow. |
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
5. Filter with `resideInPath("..commonMain..")` to exclude test code from production rules

## Harmonize (iOS/Swift)

40 tests in `iosApp/ArchitectureCheck/Tests/HarmonizeTests/`. Uses Swift Testing via SPM package.

### Test Files

| File | What It Enforces |
|------|-----------------|
| `ViewConventionsTest.swift` | SwiftUI views follow naming patterns, use correct modifiers, proper accessibility identifiers |
| `TestConventionsTest.swift` | Robot pattern (ViewTest/ViewRobot/StateRobot per View), encapsulation, landscape coverage, navint test conventions, E2E test conventions |

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
