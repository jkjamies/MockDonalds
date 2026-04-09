# core:auth

## Purpose

Authentication management for the app, split into `api` and `impl` submodules.
Features depend only on `api`; the concrete implementation is provided at runtime via Metro DI.

## Architecture

```
core/auth/api   -> AuthManager interface (feature-visible)
core/auth/impl  -> InMemoryAuthManager (DI-only, never imported directly)
```

## Public API

| Type | Module | Description |
|------|--------|-------------|
| `AuthManager` | api | Interface with `isAuthenticated: Boolean`, `login()`, `logout()` |
| `InMemoryAuthManager` | impl | `@SingleIn(AppScope)` implementation bound via `@ContributesBinding` |

## Usage

Features inject `AuthManager` by interface only:

```kotlin
class MyPresenter @Inject constructor(
    private val authManager: AuthManager,
) : Presenter<MyState> {
    // authManager.isAuthenticated / .login() / .logout()
}
```

Screens requiring authentication implement `ProtectedScreen` (from `core:circuit`).
The `AuthInterceptor` in `composeApp` checks `AuthManager.isAuthenticated` before
navigating to any `ProtectedScreen`.

## Rules

- Core modules never import from features
- Features MUST depend on `core:auth:api` only, never `core:auth:impl`
- `impl` is wired exclusively through Metro `@ContributesBinding` in `AppScope`
- Never call `login()`/`logout()` outside of auth-related presenters
- Test code should use `FakeAuthManager` from `core:test-fixtures`
