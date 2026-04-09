---
name: add-ui-tests
description: Identify and fill UI test gaps for changed screens by analyzing the branch diff. Creates UiTest, UiRobot, and StateRobot following the Robot pattern.
---

# Add UI Tests

Identify changed/new Compose UI screens and create the full Robot pattern test suite.

## Reference Standards

- Robot pattern and UI test conventions: `.agents/standards/testing-ui-component.md`
- Quality rules and shared infrastructure: `.agents/standards/testing.md`

## Scope

This skill covers Compose UI tests (Robot pattern) in `features/{name}/impl/presentation/src/androidDeviceTest/`. These tests render individual UI screens in isolation with a static `UiState`.

This skill does NOT cover navigation and integration tests in `navint-tests/`. Those tests exercise full navigation flows and Circuit presenter wiring across screens, use JUnit4 `@RunWith(AndroidJUnit4::class)`, and live in `navint-tests/src/androidDeviceTest/kotlin/`. Use `add-tests` (step 4) to evaluate navint-tests coverage when presentation or navigation modules change.

This skill also does NOT cover iOS navigation/integration tests in `iosApp/iosAppTests/NavInt/`. Those tests exercise `NavigationStateManager` state transitions, tab switching, deep links, and auth flows using Swift Testing. They are separate from iOS UI tests (which live in `iosApp/iosAppTests/{Feature}/` and use ViewInspector). Use `add-tests` (step 4) to evaluate iOS navint-tests coverage when `iosApp/iosApp/Circuit/` changes.

## Steps

### 1. Identify Test Gaps

```bash
git diff origin/main...HEAD --name-only -- '**/androidMain/**/*Ui.kt'
```

For each changed `{Feature}Ui.kt`, verify these files exist in `impl/presentation/src/androidDeviceTest/`:
- `{Feature}UiTest.kt`
- `{Feature}UiRobot.kt`
- `{Feature}StateRobot.kt`

Also verify `{Feature}TestTags.kt` exists in `api/navigation/`.

### 2. Create StateRobot

Reference: `features/order/impl/presentation/src/androidDeviceTest/.../OrderStateRobot.kt`

```kotlin
package com.mockdonalds.app.features.{name}.presentation

import com.mockdonalds.app.core.test.StateRobot

class {Feature}StateRobot : StateRobot<{Feature}UiState, {Feature}Event>() {

    override fun defaultState() = {Feature}UiState(
        // populate with representative test data
        eventSink = createEventSink(),
    )

    // Add variant states as needed:
    // fun stateWithError() = defaultState().copy(error = "...", eventSink = createEventSink())
}
```

Key: every state variant must call `createEventSink()` for a fresh event sink — do NOT reuse `eventSink` from `defaultState()`.

### 3. Create UiRobot

Reference: `features/order/impl/presentation/src/androidDeviceTest/.../OrderUiRobot.kt`

```kotlin
package com.mockdonalds.app.features.{name}.presentation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mockdonalds.app.core.theme.LocalWindowSizeClass
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.{name}.api.ui.{Feature}TestTags

class {Feature}UiRobot(private val rule: ComposeContentTestRule) {

    private val stateRobot = {Feature}StateRobot()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContentWith(state: {Feature}UiState, landscape: Boolean = false) {
        val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
        rule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
            ) {
                MockDonaldsTheme { {Feature}Ui(state = state) }
            }
        }
    }

    fun setDefaultContent() = setContentWith(stateRobot.defaultState())
    fun setLandscapeContent() = setContentWith(stateRobot.defaultState(), landscape = true)

    fun assertDefaultScreen() {
        // Assert key elements are displayed using TestTags
    }

    fun assertLandscapeScreen() {
        // Assert landscape-specific layout
    }

    fun assertLastEvent(expected: {Feature}Event) {
        org.junit.Assert.assertEquals(expected, stateRobot.lastEvent)
    }
}
```

### 4. Create UiTest

Reference: `features/order/impl/presentation/src/androidDeviceTest/.../OrderUiTest.kt`

```kotlin
package com.mockdonalds.app.features.{name}.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import io.kotest.core.spec.style.BehaviorSpec

class {Feature}UiTest : BehaviorSpec({

    val rule = createComposeRule()

    Given("the {name} screen") {
        val robot = {Feature}UiRobot(rule)

        Then("it should render the default screen correctly") {
            robot.setDefaultContent()
            robot.assertDefaultScreen()
        }

        Then("it should render the landscape layout correctly") {
            robot.setLandscapeContent()
            robot.assertLandscapeScreen()
        }
    }
})
```

### 5. Create TestTags (if missing)

Location: `features/{name}/api/navigation/src/commonMain/.../api/ui/{Feature}TestTags.kt`

```kotlin
package com.mockdonalds.app.features.{name}.api.ui

object {Feature}TestTags {
    const val SCREEN = "{name}_screen"
    // Add semantic tags for testable UI elements
}
```

### 6. AndroidManifest.xml (if missing)

Location: `features/{name}/impl/presentation/src/androidDeviceTest/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest>
    <application>
        <activity
            android:name="androidx.activity.ComponentActivity"
            android:exported="false" />
    </application>
</manifest>
```

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes. It will:

- Detect which modules have new or changed UI test files
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch Robot pattern violations, naming issues, missing landscape tests

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
