# Testing Standards

Comprehensive testing conventions for the MockDonalds KMP project. All rules are enforced by Konsist architecture tests (`architecture-check/src/test/kotlin/.../testing/`) and Harmonize (iOS).

## Test Stack

| Library | Role | Source |
|---------|------|--------|
| Kotest 6.x BehaviorSpec | Test structure (Given/When/Then) | All unit tests |
| Kotest assertions | `shouldBe`, `shouldHaveSize`, `shouldBeNull`, etc. | All unit tests |
| Turbine | Flow testing (`test { awaitItem() }`) | Presenter tests, interactor tests |
| Circuit Test | `presenterTestOf()`, `FakeNavigator` | Presenter tests |
| kotlinx-coroutines-test | `StandardTestDispatcher` (via `TestCenterPostDispatchers`) | All coroutine tests |
| Compose UI Test | `createComposeRule()`, `onNodeWithTag()`, `performClick()` | Android UI tests |
| core:test-fixtures | `TestCenterPostDispatchers`, `KotestProjectConfig`, `StateRobot` base, `FakeAuthManager` | Shared test infra |

All libraries are auto-provisioned by convention plugins (`mockdonalds.kmp.library`, `mockdonalds.kmp.presentation`). No per-module configuration needed.

## Unit Test Conventions

### Spec Style

All tests use `BehaviorSpec` (Given/When/Then). No `FunSpec`, `StringSpec`, or other Kotest styles.
Enforced by: `TestFileNamingTest` -- checks all specs in `commonTest` extend `BehaviorSpec`.

### File Placement

| Source | Test Location |
|--------|---------------|
| `features/{name}/impl/domain/*Impl.kt` | `features/{name}/impl/domain/src/commonTest/.../...ImplTest.kt` |
| `features/{name}/impl/data/*RepositoryImpl.kt` | `features/{name}/impl/data/src/commonTest/.../...RepositoryImplTest.kt` |
| `features/{name}/impl/presentation/*Presenter.kt` | `features/{name}/impl/presentation/src/commonTest/.../...PresenterTest.kt` |

Enforced by: `TestModuleCoverageTest` -- every Impl, Presenter, and RepositoryImpl has a test file.

### Coverage Requirements

Every `UseCaseImpl`, every `RepositoryImpl`, and every `Presenter` must have a corresponding test. No exceptions.

### TestCenterPostDispatchers

Always use `TestCenterPostDispatchers()` (which wraps `StandardTestDispatcher`). It routes `default`, `io`, and `main` to a single test dispatcher for deterministic execution. Never use `DefaultCenterPostDispatchers` in tests.

### Presenter Test Pattern

```kotlin
class OrderPresenterTest : BehaviorSpec({

    Given("an order presenter with content available") {
        val fakeGetOrderContent = FakeGetOrderContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(OrderScreen)

        When("the presenter emits state") {
            Then("it should start with empty defaults then populate") {
                presenterTestOf(
                    presentFunction = {
                        OrderPresenter(
                            navigator = navigator,
                            getOrderContent = fakeGetOrderContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.categories shouldBe emptyList()

                    val state = awaitItem()
                    state.categories.size shouldBe 2
                }
            }
        }

        When("the content updates") {
            Then("the presenter should emit updated state") {
                presenterTestOf(
                    presentFunction = { OrderPresenter(navigator, fakeGetOrderContent, dispatchers) },
                ) {
                    skipItems(2)
                    fakeGetOrderContent.emit(
                        FakeGetOrderContent.DEFAULT.copy(cartSummary = /*...*/)
                    )
                    val updated = awaitItem()
                    updated.cartSummary?.itemCount shouldBe 3
                }
            }
        }
    }
})
```

### Use Case Test Pattern

```kotlin
class GetHomeContentImplTest : BehaviorSpec({

    Given("a GetHomeContentImpl") {
        val repository = FakeHomeRepository()
        val impl = GetHomeContentImpl(repository)

        When("observing content") {
            Then("it should combine repository flows correctly") {
                val result = impl.createObservable(Unit).first()
                result.userName shouldBe expectedUserName
            }
        }
    }
})
```

### Repository Test Pattern

```kotlin
class OrderRepositoryImplTest : BehaviorSpec({

    Given("an OrderRepositoryImpl") {
        val repository = OrderRepositoryImpl()

        When("fetching data") {
            Then("it should return expected values") {
                val result = repository.getData().first()
                result shouldBe expectedValue
            }
        }
    }
})
```

## UI Test Conventions -- Robot Pattern

### Architecture

The robot pattern separates test orchestration from UI mechanics. Tests read like user stories, robots encapsulate Compose/ViewInspector details:

```
Android:                              iOS:
  UiTest                                ViewTest (@Suite @MainActor struct)
    └── UiRobot                           └── ViewRobot (@MainActor final class)
          ├── StateRobot                        ├── StateRobot (extends BaseStateRobot)
          └── ComposeContentTestRule            └── ViewInspector (inspect + find)
```

Android data flow:
```
UiTest calls robot.setDefaultContent()
  → UiRobot creates state via StateRobot.defaultState()
  → UiRobot wraps in MockDonaldsTheme + LocalWindowSizeClass
  → UiRobot sets ComposeContentTestRule content
  → UiTest calls robot.assertDefaultScreen()
  → UiRobot asserts via onNodeWithTag(TestTags.CONSTANT)

UiTest calls robot.tapHeroCtaButton()
  → UiRobot performs performClick() via ComposeContentTestRule
  → UiTest calls robot.assertLastEvent(expected)
  → UiRobot delegates to StateRobot.lastEvent comparison
```

iOS data flow:
```
ViewTest calls robot.assertDefaultScreen()
  → ViewRobot creates view via StateRobot.defaultState()
  → ViewRobot calls view.inspect() (ViewInspector)
  → ViewRobot asserts via find(viewWithAccessibilityIdentifier: tags.CONSTANT)

ViewTest calls robot.simulateHeroCtaTap()
  → ViewRobot invokes state.eventSink(event)
  → ViewTest calls robot.assertLastEvent(expected)
  → ViewRobot delegates to StateRobot.lastEvent comparison
```

### Three-File Structure (Android)

Every `{Feature}Ui.kt` in `androidMain` requires three files in `androidDeviceTest/`:

| File | Responsibility |
|------|---------------|
| `{Feature}StateRobot.kt` | Extends `StateRobot<UiState, Event>`. Creates states with `createEventSink()`. |
| `{Feature}UiRobot.kt` | Owns StateRobot. Wraps content in theme. Asserts UI via TestTags. |
| `{Feature}UiTest.kt` | Orchestrates scenarios through UiRobot only. |

Enforced by: `UiTestConventionsTest` -- validates Robot pattern structure, theme wrapping, landscape methods, TestTags in api.

### Android Test File Structure

```
features/{feature}/
├── api/navigation/src/commonMain/kotlin/.../ui/
│   └── {Feature}TestTags.kt              # Shared tag constants (Android + iOS)
├── impl/presentation/
│   ├── src/androidMain/kotlin/.../
│   │   └── {Feature}Ui.kt                # Compose UI (imports TestTags from api)
│   ├── src/androidDeviceTest/
│   │   ├── AndroidManifest.xml            # Declares ComponentActivity
│   │   └── kotlin/.../
│   │       ├── {Feature}StateRobot.kt     # State construction with event capture
│   │       ├── {Feature}UiRobot.kt        # UI interactions + screen assertions (owns StateRobot)
│   │       └── {Feature}UiTest.kt         # JUnit4 test class
```

### Encapsulation Rules

- `UiTest` uses `UiRobot` only -- never `StateRobot` directly
- `UiRobot` owns `StateRobot` -- single source of state creation
- All content wrapped in `MockDonaldsTheme` + `CompositionLocalProvider(LocalWindowSizeClass)`

### Required Methods

Every `UiRobot` must have:
- `setDefaultContent()` / `setLandscapeContent()` -- set content with portrait/landscape `WindowSizeClass`
- `assertDefaultScreen()` / `assertLandscapeScreen()` -- verify layout for each orientation

Every state variant must call `createEventSink()` fresh -- do NOT reuse `eventSink` from `defaultState()`.

### Theme Wrapping

```kotlin
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun setContentWith(state: HomeUiState, landscape: Boolean = false) {
    val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
    rule.setContent {
        CompositionLocalProvider(
            LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
        ) {
            MockDonaldsTheme { HomeUi(state = state) }
        }
    }
}
```

### TestTags

Located in `features/{name}/api/navigation/` as `{Feature}TestTags` objects with `const val` tags. Always use `onNodeWithTag(TestTags.CONSTANT)` -- never raw strings.

### Landscape Requirement

Every `UiTest` must have a `rendersLandscapeLayout` test that calls `setLandscapeContent()` and `assertLandscapeScreen()`.

### AndroidManifest.xml

Every feature with UI tests needs `features/{name}/impl/presentation/src/androidDeviceTest/AndroidManifest.xml` declaring `androidx.activity.ComponentActivity`.

### Event Verification

```kotlin
fun assertLastEvent(expected: HomeEvent) {
    org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
}
```

UI tests verify interactions emit correct events: tap a button, then `robot.assertLastEvent(ExpectedEvent)`.

## iOS Test Conventions

### Swift Robot Pattern (4-layer)

```
┌─────────────────────────────────────────────────────────────────┐
│                    iOS Test Architecture                         │
│                                                                  │
│  ┌───────────────────────────────┐                              │
│  │ {Feature}ViewTest             │  @Suite @MainActor struct    │
│  │   let robot = ViewRobot()     │  @Test func methods          │
│  │   robot.assertDefaultScreen() │  ONLY talks to ViewRobot     │
│  └──────────────┬────────────────┘                              │
│                 │                                                │
│  ┌──────────────▼────────────────┐                              │
│  │ {Feature}ViewRobot            │  @MainActor final class      │
│  │   let stateRobot = StateRobot │  Creates views               │
│  │   view.inspect()              │  ViewInspector assertions    │
│  │   find(viewWithAccessibility  │  createLandscapeView()       │
│  │     Identifier: tags.CONST)   │  assertLandscapeScreen()     │
│  └──────────────┬────────────────┘                              │
│                 │                                                │
│  ┌──────────────▼────────────────┐                              │
│  │ {Feature}StateRobot           │  extends BaseStateRobot      │
│  │   func defaultState()         │  State construction          │
│  │   func stateWithNoPromotion() │  Event capture               │
│  └──────────────┬────────────────┘                              │
│                 │                                                │
│  ┌──────────────▼────────────────┐                              │
│  │ BaseStateRobot<State, Event>  │  Shared base class           │
│  │   var lastEvent: Event?       │  createEventSink()           │
│  └───────────────────────────────┘                              │
└─────────────────────────────────────────────────────────────────┘
```

File structure:
```
iosApp/
├── iosAppTests/
│   ├── {Feature}/
│   │   ├── {Feature}StateRobot.swift  # State construction with event capture
│   │   ├── {Feature}ViewRobot.swift   # ViewInspector assertions + view creation (@MainActor)
│   │   └── {Feature}ViewTest.swift    # @Suite struct with @Test methods (@MainActor)
│   └── Base/
│       └── BaseStateRobot.swift       # Shared event sink + last event capture
```

| Layer | Type | Description |
|-------|------|-------------|
| `BaseStateRobot<State, Event>` | Protocol + base class | Captures events via `createEventSink()`, exposes `lastEvent` |
| `{Feature}StateRobot` | Extends BaseStateRobot | Provides `defaultState()` and variant states |
| `{Feature}ViewRobot` | `@MainActor final class` | Composes StateRobot, uses ViewInspector for assertions |
| `{Feature}ViewTest` | `@Suite @MainActor struct` | `@Test` methods orchestrating ViewRobot |

Enforced by: Harmonize `TestConventionsTest.swift` -- validates every View has ViewTest/ViewRobot/StateRobot, robot encapsulation, landscape coverage.

### Swift Testing Framework

iOS tests use Swift Testing (`@Suite`, `@Test`), not XCTest. ViewRobots use ViewInspector for SwiftUI inspection. SwiftLint applies to test files (config at `.swiftlint.yml`).

## Navigation & Integration Tests (navint-tests)

### Purpose

`navint-tests` verifies end-to-end navigation flows and cross-screen integration using real Circuit presenters backed by a fake data layer. Unlike unit tests (which test individual classes in isolation) and UI tests (which render a single screen with a static state), navint-tests exercise the full presenter-navigation contract across multiple screens.

### Key Characteristics

- **Runner**: JUnit4 `@RunWith(AndroidJUnit4::class)` — instrumented tests, NOT Kotest BehaviorSpec
- **Data layer**: Fake implementations only — no `impl/domain` or `impl/data` modules wired in
- **Presenters**: Real Circuit presenters (same as production)
- **Location**: `navint-tests/src/androidDeviceTest/kotlin/`
- **File naming**: ends with `NavigationTest.kt` or `IntegrationTest.kt`
- **Run command**: `./gradlew :navint-tests:connectedAndroidDeviceTest` (requires connected emulator)

### When to Add navint-tests

Add or update navint-tests when:
- A new navigation flow is introduced between screens
- An existing screen's navigation events change (`api/navigation/`)
- A presenter starts navigating to a new destination
- An auth-gated flow (`ProtectedScreen`) is added or modified

### Test Structure

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeToOrderNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun navigatingFromHomeToOrder_showsOrderScreen() {
        // Set up fake use cases and Circuit presenter
        // Drive navigation event
        // Assert destination screen is shown
    }
}
```

### Distinction from UI Tests

| | UI Tests (Robot pattern) | navint-tests |
|---|---|---|
| Location | `impl/presentation/src/androidDeviceTest/` | `navint-tests/src/androidDeviceTest/` |
| Scope | Single screen, static UiState | Multi-screen navigation flows |
| Runner | JUnit4 (Compose UI test) | JUnit4 `@RunWith(AndroidJUnit4::class)` |
| Presenters | Not involved (state injected directly) | Real Circuit presenters |
| Data layer | Not involved | Fakes only |
| File naming | `{Feature}UiTest.kt` | `{Flow}NavigationTest.kt` / `{Feature}IntegrationTest.kt` |

## iOS Navigation & Integration Tests (iosAppTests/NavInt)

### Purpose

iOS navint-tests verify navigation state management on the iOS side using `NavigationStateManager` (extracted from `CircuitNavigator.swift`). Unlike iOS UI tests (which render SwiftUI views with ViewInspector), these tests exercise state transitions, tab switching, deep link routing, and auth flow navigation without rendering any views.

### Test Plans

Three Xcode test plans organize all 66 iOS tests:

| Test Plan | Tests | Scope |
|-----------|-------|-------|
| `AllTests.xctestplan` | 66 | Default — runs all unit + navint tests |
| `UnitTests.xctestplan` | 42 | Component-level tests (ViewTest, StateRobot-based) |
| `NavIntTests.xctestplan` | 24 | Navigation & integration tests only |

### Key Characteristics

- **Framework**: Swift Testing (`@Suite @MainActor struct`) — NOT XCTest
- **State under test**: `NavigationStateManager` — extracted, testable navigation state handling
- **No ViewInspector**: These tests verify state management, not view rendering
- **Location**: `iosApp/iosAppTests/NavInt/`
- **Run command**: `xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'`

### Test Suites

| Suite | Purpose |
|-------|---------|
| `NavigationStateManagerTest` | Core state manager behavior — initialization, state transitions |
| `TabSwitchingTest` | Tab selection, tab state persistence |
| `DeepLinkNavigationTest` | Deep link URL routing to correct screens/tabs |
| `AuthFlowNavigationTest` | Auth-gated navigation, login/logout state transitions |

### When to Add iOS navint-tests

Add or update iOS navint-tests when:
- `NavigationStateManager` logic changes (`iosApp/iosApp/Circuit/`)
- New tabs or navigation destinations are added on iOS
- Deep link handling is added or modified
- Auth-gated navigation flows change on iOS

### File Naming

Test files in `iosApp/iosAppTests/NavInt/` end with `Test.swift` and use descriptive names matching the navigation concern being tested.

### Distinction from iOS UI Tests

| | iOS UI Tests (Robot pattern) | iOS navint-tests |
|---|---|---|
| Location | `iosAppTests/{Feature}/` | `iosAppTests/NavInt/` |
| Scope | Single view, static state | Navigation state flows |
| Framework | Swift Testing + ViewInspector | Swift Testing (no ViewInspector) |
| State source | `StateRobot.defaultState()` | `NavigationStateManager` |
| What they test | View rendering + interactions | State transitions + routing |

### Enforcement

4 Harmonize rules in `iosApp/ArchitectureCheck/` enforce navint test conventions (naming, location, structure, Swift Testing usage).

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
