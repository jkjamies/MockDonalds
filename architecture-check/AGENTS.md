# Konsist Architecture Tests

## Purpose

18 architecture test classes enforcing project conventions via Konsist compile-time static analysis.
All tests use Kotest BehaviorSpec (Given/Then style) and scan the project with `Konsist.scopeFromProject()`.

## Test Categories

| Category | Tests | What They Enforce |
|----------|-------|-------------------|
| architecture/ | LayerDependencyTest | Unidirectional dependency flow: api <- domain <- data, api <- presentation. Cross-feature imports only via api module. Core modules cannot import features. |
| architecture/ | CircularDependencyTest | No circular api imports between feature modules (A -> B and B -> A). |
| architecture/ | ForbiddenPatternsTest | No ViewModels (use Circuit presenters), no raw CoroutineScope/launch/async/Dispatchers in features (use CenterPost), no Android platform imports in commonMain, no app module imports from library modules. |
| circuit/ | CircuitConventionsTest | Events must be sealed class (not interface) for iOS interop. Screen objects reside in api with @Parcelize. TabScreens have tag property. ProtectedScreens in api/navigation. |
| circuit/ | NamingConventionsTest | Screens end with Screen, events with Event, @CircuitInject functions with Presenter or Ui, UiState classes with UiState, repository interfaces with Repository, implementations with Impl/RepositoryImpl. |
| core/ | CodeHygieneTest | No wildcard imports, no println/System.out in production, no Thread.sleep or runBlocking, no force unwraps (!!), no lateinit var in shared code. |
| core/ | DependencyInjectionTest | Every Repository interface has a @ContributesBinding implementation. Every abstract use case has a @ContributesBinding Impl. @CircuitInject presenters also have @Inject. |
| core/ | PackageConventionsTest | Feature files use com.mockdonalds.app.features.* packages. Core files use com.mockdonalds.app.core.* packages. Package segments match module path. |
| core/ | VisibilityConventionsTest | @ContributesBinding classes are public. Domain modules expose only Repository interfaces and Impl classes. UiState classes are not internal. |
| core/ | AgentDocumentationTest | Every feature and core module has AGENTS.md. Root AGENTS.md exists. .agents/skills/ directory exists with SKILL.md per skill. .gemini/settings.json references AGENTS.md. |
| layers/ | ApiLayerTest | Data classes in api are immutable (val only). @Serializable only in api/data/network. No MutableStateFlow in public APIs. api:domain has no Circuit dependency. DTOs only in data/network modules. |
| layers/ | DataLayerTest | Repository interfaces in domain modules. RepositoryImpl classes in data modules with @ContributesBinding. Repository functions return Flow, not suspend. |
| layers/ | DomainLayerTest | Abstract use cases (CenterPostInteractor/CenterPostSubjectInteractor) in api modules. Impl classes in domain modules with @ContributesBinding, extending their abstract parent. |
| layers/ | PresentationLayerTest | Presenters have @CircuitInject. One public function per presenter file. Presenters do not depend on repositories directly. UiState implements CircuitUiState with eventSink property. |
| testing/ | TestDoubleConventionsTest | Fakes live in dedicated test modules (not commonTest). Every abstract use case has a Fake. Test doubles prefixed with Fake or Test. No mockk in commonTest. |
| testing/ | TestFileNamingTest | Test classes end with Test/Tests. All specs extend BehaviorSpec. No runBlocking, runTest, or UnconfinedTestDispatcher in tests. |
| testing/ | TestModuleCoverageTest | Every feature has a dedicated test module. Every use case Impl, presenter, and RepositoryImpl has a corresponding test file. |
| testing/ | UiTestConventionsTest | Every *Ui.kt has a *UiTest in androidDeviceTest. Robot pattern: UiTest -> UiRobot -> StateRobot. UiRobots wrap in MockDonaldsTheme, provide LocalWindowSizeClass, have landscape methods. TestTags objects in api module. |

## Running

```bash
./gradlew :architecture-check:test
```

## Adding New Rules

1. Create a new `BehaviorSpec` in the appropriate package under `architecture-check/src/test/kotlin/com/mockdonalds/app/konsist/`
2. Use `Konsist.scopeFromProject()` for project-wide checks
3. Use `resideInPath("..impl/domain..")` for module-scoped checks
4. Use `Konsist.scopeFromSourceSet("commonMain", "features..", "domain")` for source-set-scoped checks
5. Filter with `resideInPath("..commonMain..")` to exclude test code from production rules
