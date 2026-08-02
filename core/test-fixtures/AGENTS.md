# core:test-fixtures

## Purpose

Shared test infrastructure for all feature test modules. Provides fakes, base classes,
and Kotest configuration. Auto-included in all test modules via convention plugins.

## Public API

| Type | Description |
|------|-------------|
| `TestStrataDispatchers` | Implements `StrataDispatchers` routing all dispatchers (`default`, `io`, `main`) to a single `StandardTestDispatcher`. Accepts an optional custom `TestDispatcher`. |
| `KotestProjectConfig` | Open class extending `AbstractProjectConfig`. Sets `specExecutionMode` to `LimitedConcurrency(4)` for parallel test execution. |
| `StateRobot<State, Event>` | Abstract base class for Circuit presenter testing robots. Captures events via `createEventSink()`, exposes `capturedEvents`, `lastEvent`, and `clearEvents()`. Subclass implements `defaultState()`. |
| `FakeAuthManager` | Test fake implementing `AuthManager`. Constructor accepts initial `isAuthenticated`, `tokens`, and `refreshResult`. Tracks `refreshCallCount`. |
| `FakeRefreshTokenSource` | Test fake implementing `RefreshTokenSource`. Configurable `result` / `error`, tracks `callCount`, supports `holdNextCallsUntilReleased()` / `release()` for deterministic concurrency tests. |
| `FakeSensorDataProvider` | Test fake implementing `SensorDataProvider`. Mutable `data` property; `currentSensorData()` returns it. |

## Usage

### Testing a StrataInteractor

```kotlin
class GetMenuInteractorTest : FunSpec({
    val dispatchers = TestStrataDispatchers()

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
- All test modules must use `TestStrataDispatchers` -- never `DefaultStrataDispatchers`
- All presenter tests should use `StateRobot` for event capture
- Use `FakeAuthManager` / `FakeRefreshTokenSource` instead of mocking `AuthManager` / `RefreshTokenSource`
- Do not add production code to this module -- test infrastructure only
