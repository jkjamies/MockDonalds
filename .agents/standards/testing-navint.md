# Navigation & Integration Testing Standards

Navigation/integration (navint) tests verify cross-screen navigation flows and shared state propagation using real presenters backed by a fake data layer. Unlike unit tests (single class) and UI component tests (single screen, static state), navint-tests exercise the full presenter-navigation contract across multiple screens.

> Shared conventions (test stack, quality standards, fakes, infrastructure) are in [testing.md](testing.md).

## Scope

| What's tested | What's real | What's faked |
|---------------|-------------|--------------|
| Multi-screen navigation flows, cross-feature state | Circuit presenters, navigation, UI | Data layer (use cases via fakes from `test/` modules) |

## Android navint-tests

### Run Commands

```bash
# All Android navint tests (requires emulator)
./gradlew :navint-tests:connectedAndroidDeviceTest
```

### Key Characteristics

- **Runner**: JUnit4 `@RunWith(AndroidJUnit4::class)` — instrumented tests, NOT Kotest BehaviorSpec
- **Data layer**: Fake implementations only — no `impl/domain` or `impl/data` modules wired in
- **Presenters**: Real Circuit presenters (same as production)
- **Location**: `navint-tests/src/androidDeviceTest/kotlin/`
- **File naming**: ends with `NavigationTest.kt` or `IntegrationTest.kt`

### When to Add

Add or update Android navint-tests when:
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

### Module Dependencies

```
navint-tests depends on:
  features/*/api/domain       — public models, abstract use cases
  features/*/api/navigation   — Screen objects, TestTags
  features/*/impl/presentation — real presenters + UI
  features/*/test             — fakes with @ContributesBinding
  core:circuit, core:metro, core:auth:api, core:theme, core:test-fixtures

navint-tests does NOT depend on:
  features/*/impl/domain      — fakes replace real implementations
  features/*/impl/data        — fakes replace real implementations
```

## iOS navint-tests

### Run Commands

```bash
# iOS navint tests (requires simulator)
xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'
```

### Key Characteristics

- **Framework**: Swift Testing (`@Suite @MainActor struct`) — NOT XCTest
- **State under test**: `NavigationStateManager` — extracted, testable navigation state handling
- **No ViewInspector**: These tests verify state management, not view rendering
- **Location**: `iosApp/iosAppTests/NavInt/`

### Test Plans

Three Xcode test plans organize all 66 iOS tests:

| Test Plan | Tests | Scope |
|-----------|-------|-------|
| `AllTests.xctestplan` | 66 | Default — runs all unit + navint tests |
| `UnitTests.xctestplan` | 42 | Component-level tests (ViewTest, StateRobot-based) |
| `NavIntTests.xctestplan` | 24 | Navigation & integration tests only |

### Test Suites

| Suite | Purpose |
|-------|---------|
| `NavigationStateManagerTest` | Core state manager behavior — initialization, state transitions |
| `TabSwitchingTest` | Tab selection, tab state persistence |
| `DeepLinkNavigationTest` | Deep link URL routing to correct screens/tabs |
| `AuthFlowNavigationTest` | Auth-gated navigation, login/logout state transitions |

### When to Add

Add or update iOS navint-tests when:
- `NavigationStateManager` logic changes (`iosApp/iosApp/Circuit/`)
- New tabs or navigation destinations are added on iOS
- Deep link handling is added or modified
- Auth-gated navigation flows change on iOS

### File Naming

Test files in `iosApp/iosAppTests/NavInt/` end with `Test.swift` and use descriptive names matching the navigation concern being tested.

## Distinction from Other Test Levels

| | Unit Tests | UI Component Tests | navint-tests | e2e-tests |
|---|---|---|---|---|
| Location | `impl/*/commonTest/` | `impl/presentation/androidDeviceTest/` | `navint-tests/` or `iosAppTests/NavInt/` | `e2e-tests/` |
| Scope | Single class | Single screen | Multi-screen flows | Full user journeys |
| Data | Fakes | Static state | Fakes | Real |
| Presenters | Isolated | Not involved | Real | Real |
| Navigation | Not involved | Not involved | Real Circuit / NavigationStateManager | Real |

## Enforcement

### Konsist (Android)
- `TestBoundaryTest` -- navint-tests must not import feature-level UiRobot/StateRobot or from impl/domain or impl/data

### Harmonize (iOS)
- 4 rules in `TestConventionsTest.swift` -- navint tests must be `@Suite` structs, `@MainActor`, must not import ViewInspector, must not import feature-level robots
