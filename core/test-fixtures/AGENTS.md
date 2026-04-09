# core:test-fixtures

## Purpose

Shared test infrastructure for all feature test modules. Provides fakes, base classes,
and Kotest configuration. Auto-included in all test modules via convention plugins.

## Public API

| Type | Description |
|------|-------------|
| `TestCenterPostDispatchers` | Implements `CenterPostDispatchers` routing all dispatchers (`default`, `io`, `main`) to a single `StandardTestDispatcher`. Accepts an optional custom `TestDispatcher`. |
| `KotestProjectConfig` | Open class extending `AbstractProjectConfig`. Sets `specExecutionMode` to `LimitedConcurrency(4)` for parallel test execution. |
| `StateRobot<State, Event>` | Abstract base class for Circuit presenter testing robots. Captures events via `createEventSink()`, exposes `capturedEvents`, `lastEvent`, and `clearEvents()`. Subclass implements `defaultState()`. |
| `FakeAuthManager` | Test fake implementing `AuthManager`. Constructor accepts initial `isAuthenticated` (default `false`). Provides working `login()`/`logout()` toggling state. |

## Usage

### Testing a CenterPostInteractor

```kotlin
class GetMenuInteractorTest : FunSpec({
    val dispatchers = TestCenterPostDispatchers()

    test("returns menu items") {
        val interactor = GetMenuInteractor(FakeMenuRepository())
        val result = interactor(Unit)
        result.onSuccess { items ->
            items.shouldNotBeEmpty()
        }
    }
})
```

### Testing a presenter with StateRobot

```kotlin
class MenuRobot : StateRobot<MenuState, MenuEvent>() {
    override fun defaultState() = MenuState(items = emptyList())

    fun verifyItemsLoaded(count: Int) { /* assertions */ }
}
```

### Kotest project config

Each test module creates a `ProjectConfig` class:

```kotlin
class ProjectConfig : KotestProjectConfig()
```

## Rules

- Core modules never import from features
- All test modules must use `TestCenterPostDispatchers` -- never `DefaultCenterPostDispatchers`
- All presenter tests should use `StateRobot` for event capture
- Use `FakeAuthManager` instead of mocking `AuthManager`
- Do not add production code to this module -- test infrastructure only
