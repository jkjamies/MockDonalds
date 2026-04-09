# UI Component Testing Standards

UI component tests verify individual screen rendering and user interactions using the Robot pattern. Tests inject static state into a single screen and assert elements render correctly and interactions emit correct events. No presenters, no navigation, no data layer.

> Shared conventions (test stack, quality standards, fakes, infrastructure) are in [testing.md](testing.md).

## Scope

| What's tested | What's real | What's faked |
|---------------|-------------|--------------|
| Single screen's Compose UI / SwiftUI View | UI rendering, event dispatch | UiState (static, constructed by StateRobot) |

## Run Commands

```bash
# Android UI tests (requires emulator)
./gradlew :features:{name}:impl:presentation:connectedAndroidDeviceTest  # Single feature
./gradlew connectedAndroidDeviceTest                                      # All features

# iOS UI tests (requires simulator)
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'
```

## Android Robot Pattern

### Architecture

The robot pattern separates test orchestration from UI mechanics. Tests read like user stories, robots encapsulate Compose details:

```
UiTest
  └── UiRobot
        ├── StateRobot
        └── ComposeContentTestRule
```

Data flow:
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

### Three-File Structure

Every `{Feature}Ui.kt` in `androidMain` requires three files in `androidDeviceTest/`:

| File | Responsibility |
|------|---------------|
| `{Feature}StateRobot.kt` | Extends `StateRobot<UiState, Event>`. Creates states with `createEventSink()`. |
| `{Feature}UiRobot.kt` | Owns StateRobot. Wraps content in theme. Asserts UI via TestTags. |
| `{Feature}UiTest.kt` | Orchestrates scenarios through UiRobot only. |

Enforced by: `UiTestConventionsTest` -- validates Robot pattern structure, theme wrapping, landscape methods, TestTags in api.

### File Structure

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

Located in `features/{name}/api/navigation/` as `{Feature}TestTags` objects with `const val` tags. Always use `onNodeWithTag(TestTags.CONSTANT)` -- never raw strings. Shared across Android and iOS.

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

## iOS Robot Pattern (4-layer)

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

iOS UI component tests use Swift Testing (`@Suite`, `@Test`), not XCTest. ViewRobots use ViewInspector for SwiftUI inspection. SwiftLint applies to test files (config at `.swiftlint.yml`).

## Enforcement

- `UiTestConventionsTest` (Konsist) -- Robot pattern structure, theme wrapping, landscape methods, TestTags in api
- `TestConventionsTest.swift` (Harmonize) -- ViewTest/ViewRobot/StateRobot per View, encapsulation, landscape
- `TestBoundaryTest` (Konsist) -- UI tests must not use real Navigator or call resetRoot()/goTo()
- `TestModuleCoverageTest` (Konsist) -- every `{Feature}TestTags` must also appear in e2e-tests (cross-level coverage)
- `TestConventionsTest.swift` (Harmonize) -- every `{Feature}View` must also appear in iOS e2e tests (cross-level coverage)
