# core:network

## Purpose

Per-feature Ktor HTTP client factory with baked-in platform infrastructure. Each feature/domain team creates its own `HttpClient` via a DSL builder. Core owns the HTTP machinery and non-negotiable plugins; features own their base URL, endpoints, DTOs, and feature-specific config.

## Module Structure

```
core/network/
  api/   — HttpClientFactory, ClientConfig DSL, NetworkException, AuthMode
  impl/  — HttpClientFactoryImpl (baked-in plugins), JsonProvider
```

## Public API (api module)

| Type | Description |
|------|-------------|
| `HttpClientFactory` | `fun interface` — creates per-feature `HttpClient` instances via DSL config |
| `ClientConfig` | DSL class — `baseUrl`, `authMode`, `requestTimeout`, `connectTimeout`, `socketTimeout`, `headers`, `ktorConfig` escape hatch |
| `AuthMode` | Enum — `BEARER` (default, future: auto-refresh on 401) or `NONE` (public endpoints) |
| `NetworkException` | Sealed class — `HttpError`, `Timeout`, `NoConnectivity`, `Serialization`, `Unknown` |

## Implementation (impl module)

| Type | Description |
|------|-------------|
| `HttpClientFactoryImpl` | `@ContributesBinding` factory. Bakes in: JSON content negotiation, `X-App-Id` header, `X-Market` header, dev logging (non-prod), `HttpTimeout`. Features configure via DSL. |
| `JsonProvider` | `@ContributesTo` interface providing singleton `Json` instance with `ignoreUnknownKeys`, `isLenient`, `encodeDefaults`, `explicitNulls = false` |

## Usage

Consumed by `impl/data` modules. Each feature creates its own client:

```kotlin
@ContributesBinding(AppScope::class)
@Inject
class MenuRepositoryImpl(
    httpClientFactory: HttpClientFactory,
    appBuildConfig: AppBuildConfig,
) : MenuRepository {
    private val client = httpClientFactory.create {
        baseUrl = appBuildConfig.menuBaseUrl
        requestTimeout = 10.seconds
    }

    override fun getMenuItems(): Flow<List<MenuItem>> = flowOf(/* ... */)
}
```

Per-service base URLs come from `AppBuildConfig` market properties (`menuBaseUrl`, `orderBaseUrl`, etc.).

## Baked-In Plugins (non-negotiable)

- **ContentNegotiation** — JSON via kotlinx.serialization
- **X-App-Id header** — per-market app identifier from `AppBuildConfig`
- **X-Market header** — market code from `AppBuildConfig`
- **HttpTimeout** — defaults: request 15s, connect 5s, socket 10s (overridable via DSL)
- **Logging** — `LogLevel.HEADERS` in non-prod, `LogLevel.NONE` in prod

## Future (stubbed/planned)

- **Auth (bearer + refresh)** — `AuthMode.BEARER` is declared but auth plugin not yet wired. Will integrate with `core:auth` `AuthManager` for token injection and 401 refresh with `Mutex`-coordinated concurrent refresh.
- **Akamai headers** — always-on, every request. Plugin placeholder for SDK integration.
- **Retry with backoff** — configurable retry policy.
- **Error mapping** — HTTP errors mapped to `NetworkException` subtypes.

## Rules

- Core modules never import from features
- Only `impl/data` modules should depend on `core:network:api` (plus `core:network:impl` itself)
- `composeApp` depends on `core:network:impl` (DI graph wiring)
- Presenter and UI modules must NEVER import or use `HttpClient` directly
- All network calls must go through a repository → data source, consumed via `CenterPostInteractor` in the presenter layer
- Each feature creates its own client — no shared singleton `HttpClient`
- Ktor engine is resolved automatically per platform (OkHttp on Android, Darwin on iOS)
