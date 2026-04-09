# core:circuit

## Purpose

Shared Circuit runtime screen types and KMP annotations used across all feature modules.
Defines marker interfaces for tab-based navigation and authentication gating.

## Public API

| Type | Description |
|------|-------------|
| `TabScreen` | Interface extending Circuit `Screen`. Declares `val tag: String` used for tab identity in bottom navigation. |
| `ProtectedScreen` | Marker interface extending Circuit `Screen`. Screens implementing this are auth-gated by `AuthInterceptor` in `composeApp`. |
| `@Parcelize` | Expect/actual annotation (`@Target(CLASS)`) for KMP-compatible parcelization of screen data classes. |

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

### Combining both

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
- Screen definitions live in the feature's `api` module, not here
