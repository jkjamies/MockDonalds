# core:circuit

## Purpose

Shared Circuit runtime screen types and KMP annotations used across all feature modules.
Defines marker interfaces for tab-based navigation and authentication gating.

## Public API

| Type | Description |
|------|-------------|
| `TabScreen` | Interface extending Circuit `Screen`. Declares `val tag: String` used for tab identity in bottom navigation. |
| `ProtectedScreen` | Marker interface extending Circuit `Screen`. Screens implementing this are auth-gated by `AuthInterceptor` in `composeApp`. |
| `FlowScreen` | Marker interface extending Circuit `Screen`. Screens implementing this are presented as a nested flow on iOS (fullScreenCover with inner NavigationStack). On Android, handled by Circuit's nested navigation in the Compose UI layer. |
| `@Parcelize` | Expect/actual annotation (`@Target(CLASS)`) for KMP-compatible parcelization of screen data classes. |
| `CircuitProviders` | `@ContributesTo(AppScope)` interface. Aggregates `Presenter.Factory` and `Ui.Factory` multibindings into a `Circuit` instance via `Circuit.Builder()`. |

## Usage

### Defining a tab screen

```kotlin
@Parcelize
data object MenuScreen : TabScreen {
    override val tag: String = "menu"
}
```

### Defining an auth-gated screen

```kotlin
@Parcelize
data object RewardsScreen : ProtectedScreen
```

### Defining a flow screen

```kotlin
@Parcelize
data class LoginScreen(val returnTo: Screen? = null) : FlowScreen
```

On iOS, `BridgeNavigator` detects `FlowScreen` and presents it as a `.fullScreenCover` with its own inner `NavigationStack`. Inner screens pushed within the flow are regular Circuit screens — their `goTo()`/`pop()` calls route to the flow's inner path. On Android, `FlowScreen` is a regular screen on the backstack — its Compose UI uses Circuit's nested `CircuitContent(onNavEvent)` for inner screen navigation.

### Combining markers

```kotlin
@Parcelize
data object OrderScreen : TabScreen, ProtectedScreen {
    override val tag: String = "order"
}
```

The `AuthInterceptor` in `composeApp` checks `AuthManager.isAuthenticated` for any
screen that implements `ProtectedScreen` before allowing navigation.

## Rules

- Core modules never import from features
- All Circuit `Screen` data classes/objects must use `@Parcelize`
- Tab screens MUST implement `TabScreen` and provide a unique `tag`
- Auth-gated screens MUST implement `ProtectedScreen`
- Multi-screen flow root screens MUST implement `FlowScreen`
- Screen definitions live in the feature's `api` module, not here
