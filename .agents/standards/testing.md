# Testing Standards

Comprehensive testing conventions for the MockDonalds KMP project. Testing is organized into 5 levels, each with its own standards document. All rules are enforced by Konsist architecture tests (`testing/architecture-check/`) and Harmonize (`iosApp/ArchitectureCheck/`).

## Test Levels

| Level | Standard | Scope | Run Command |
|-------|----------|-------|-------------|
| Unit | [testing-unit.md](testing-unit.md) | Single class (use case, repository, presenter) — Kotlin on host + Swift pure-logic | `./gradlew testAndroidHostTest` / `xcodebuild test -testPlan UnitTests` |
| UI Component | [testing-ui-component.md](testing-ui-component.md) | Single screen rendering + interactions (Robot pattern) | `./gradlew connectedAndroidDeviceTest` / `xcodebuild test -testPlan UIComponentTests` |
| Navigation & Integration | [testing-navint.md](testing-navint.md) | Multi-screen navigation flows, cross-feature state | `./gradlew :testing:navint-tests:connectedAndroidDeviceTest` / `xcodebuild test -testPlan NavIntTests` |
| End-to-End | [testing-e2e.md](testing-e2e.md) | Full user journeys against real app | `./gradlew :testing:e2e-tests:connectedAndroidTest` |
| Architecture | [testing-architecture.md](testing-architecture.md) | Source structure, imports, annotations, naming | `./gradlew :testing:architecture-check:test` / `swift test --package-path iosApp/ArchitectureCheck` |

## Test Level Comparison

| | Unit | UI Component | navint | e2e | Architecture |
|---|---|---|---|---|---|
| Location | `impl/*/commonTest/` + `iosAppTests/Unit/` | `impl/presentation/androidDeviceTest/` + `iosAppTests/UIComponent/` | `testing/navint-tests/` or `iosAppTests/NavInt/` | `testing/e2e-tests/` | `testing/architecture-check/` or `ArchitectureCheck/` |
| Data | Fakes | Static state | Fakes | Real | N/A |
| Presenters | Isolated | Not involved | Real | Real | N/A |
| Navigation | Not involved | Not involved | Real | Real | N/A |
| Element access | N/A | Compose test / ViewInspector | Compose test / state assertions | UI Automator | Static analysis |
| Framework | Kotest BehaviorSpec | JUnit4 + Compose / Swift Testing + ViewInspector | JUnit4 + Compose / Swift Testing | JUnit4 + UI Automator | Konsist + Harmonize |
| Requires device | No | Yes (emulator/simulator) | Yes (emulator/simulator) | Yes (device/emulator) | No |

## Test Stack

| Library | Role | Used By |
|---------|------|---------|
| Kotest 6.x BehaviorSpec | Test structure (Given/When/Then) | Unit tests |
| Kotest assertions | `shouldBe`, `shouldHaveSize`, `shouldBeNull`, etc. | Unit tests |
| Turbine | Flow testing (`test { awaitItem() }`) | Presenter tests, interactor tests |
| Circuit Test | `presenterTestOf()`, `FakeNavigator` | Presenter tests |
| kotlinx-coroutines-test | `StandardTestDispatcher` (via `TestCenterPostDispatchers`) | All coroutine tests |
| Compose UI Test | `createComposeRule()`, `onNodeWithTag()`, `performClick()` | UI component tests, navint-tests |
| UI Automator | `By.desc(testTag)`, cross-process element access | e2e-tests |
| Macrobenchmark | `MacrobenchmarkRule`, `StartupTimingMetric` | e2e benchmarks |
| Swift Testing | `@Suite`, `@Test`, `@MainActor` | iOS unit tests, iOS navint-tests |
| ViewInspector | SwiftUI view hierarchy traversal | iOS UI component tests |
| Konsist | Kotlin static analysis | Architecture tests |
| Harmonize | Swift static analysis | iOS architecture tests |
| core:test-fixtures | `TestCenterPostDispatchers`, `KotestProjectConfig`, `StateRobot` base, `FakeAuthManager` | Shared test infra |

All libraries are auto-provisioned by convention plugins (`mockdonalds.kmp.library`, `mockdonalds.kmp.presentation`). No per-module configuration needed.

## Test Quality Standards

1. **No change detectors** -- tests must verify behavior, not mirror implementation. Asserting a function returns the exact hardcoded value it was given is not a test. Test state transitions, error paths, boundary conditions.

2. **No magic numbers** -- use named constants or derive expected values. `result.points shouldBe 1500` is meaningless; `result.points shouldBe initialPoints + earnedPoints` tests logic.

3. **No testing standard library** -- do not test that `Flow.map` transforms, `combine()` combines, or `MutableStateFlow` emits. Test YOUR logic that uses them.

4. **No testing fakes** -- fakes are test infrastructure. Do not write tests verifying fake behavior.

5. **Test state transitions, not snapshots** -- a presenter test should verify that emitting new content from a fake causes UiState to update, not just that initial state has defaults.

6. **Presenter tests must test events** -- emit content via fakes, send events via `eventSink`, assert state changes and `FakeNavigator` navigation calls. Initial-state-only tests are insufficient.

7. **UI tests must verify interactions** -- `StateRobot` creates states, `UiRobot` sets content and asserts elements via TestTags, tests should cover default, variant (empty, error, loaded), and interaction scenarios (taps emit correct events via `assertLastEvent()`).

## Test Infrastructure Overview

```
core/test-fixtures/                    # Auto-added to all modules via convention plugin
├── TestCenterPostDispatchers          # Routes all dispatchers to TestDispatcher
├── KotestProjectConfig                # Base config for concurrent spec execution (4 specs)
├── StateRobot<State, Event>           # Base class for UI test state construction
└── FakeAuthManager                    # Shared auth fake

features/{feature}/test/               # Feature-specific fakes (commonMain, NOT commonTest)
└── Fake{UseCase}                      # MutableStateFlow-backed, extends abstract use case
      ├── emit(content)                # Test control
      └── createObservable(params)     # Returns the MutableStateFlow
```

Dependency flow for tests:
```
impl/presentation/commonTest/
  ├── depends on: features/{feature}/test (fakes)
  ├── depends on: core:test-fixtures (TestCenterPostDispatchers, KotestProjectConfig)
  └── depends on: circuit-test (presenterTestOf, FakeNavigator)

impl/domain/commonTest/
  └── uses inline fakes for repository interfaces (repo is internal to domain/data boundary)

impl/data/commonTest/
  └── tests repository implementations directly
```

## Fakes

### Location

Fakes live in `features/{name}/test/src/commonMain/` -- NOT in `commonTest/`. This makes them available to any module that depends on the test module.
Enforced by: `TestDoubleConventionsTest`.

### Implementation Pattern

```kotlin
@ContributesBinding(AppScope::class)
class FakeGetOrderContent @Inject constructor(
    initial: OrderContent = DEFAULT,
) : GetOrderContent() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<OrderContent> = _content

    fun emit(content: OrderContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = OrderContent(/* representative test data */)
    }
}
```

### Rules

- **No mockk/Mockito** -- banned for thread safety. Kotest runs specs with `SpecExecutionMode.LimitedConcurrency(4)`; mocking libraries are not thread-safe under concurrent execution. Fakes with `MutableStateFlow` give precise control and are inherently thread-safe.
  Enforced by: `TestDoubleConventionsTest` -- no `io.mockk` imports in commonTest.

- **Every abstract use case gets a Fake** -- if `GetHomeContent` exists in `api/domain`, `FakeGetHomeContent` must exist in `test/src/commonMain/`.
  Enforced by: `TestDoubleConventionsTest`.

- **Naming** -- test doubles must be prefixed with `Fake` or `Test`.
  Enforced by: `TestDoubleConventionsTest`.

- **MutableStateFlow-backed** -- fakes expose `emit()` for test control. The flow is the single source of truth.
