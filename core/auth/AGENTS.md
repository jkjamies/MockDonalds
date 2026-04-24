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
| `AuthManager` | api | Interface — `isAuthenticated: Boolean`, `login()`, `logout()`, `suspend currentTokens(): AuthTokens?`, `suspend refresh(): AuthTokens?` |
| `AuthTokens` | api | `data class` — `accessToken`, `refreshToken` |
| `RefreshTokenSource` | api | `suspend fun refresh(refreshToken: String): AuthTokens?` — callable by `AuthManager.refresh()`; production impl swaps the stub for a real refresh endpoint |
| `InMemoryAuthManager` | impl | `@SingleIn(AppScope)` `AuthManager` binding. Holds tokens in-memory; `refresh()` serializes concurrent callers via `Mutex` + `CompletableDeferred` so cross-client 401 bursts collapse to one upstream refresh |
| `StubRefreshTokenSource` | impl | `@SingleIn(AppScope)` `RefreshTokenSource` binding. Returns `null`; present so the DI graph resolves until a real refresh client is wired |

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
- Test code should use `FakeAuthManager` and `FakeRefreshTokenSource` from `core:test-fixtures`
