# core:network

## Purpose

Per-feature Ktor HTTP client factory with baked-in platform infrastructure. Each feature/domain team creates its own `HttpClient` via a DSL builder. Core owns the HTTP machinery and non-negotiable plugins; features own their base URL, endpoints, DTOs, and feature-specific config.

## Module Structure

```
core/network/
  api/
    commonMain/   — HttpClientFactory, ClientConfig DSL, NetworkException, AuthMode, SensorDataProvider
    iosMain/      — AkamaiSensorBridge (Kotlin contract implemented in Swift)
  impl/
    commonMain/   — HttpClientFactoryImpl (baked-in plugins), HttpClientProvider, JsonProvider
    androidMain/  — AkamaiSensorDataProviderAndroid (real SDK drop-in point)
    iosMain/      — AkamaiSensorDataProviderIos (delegates to AkamaiSensorBridge supplied via ProdAppGraph.Factory)
```

`AkamaiSensorBridge` lives in `api/iosMain` so it exports to Swift as `ApiAkamaiSensorBridge` (Kotlin/Native module-prefix convention for transitively-visible types). `SwiftAkamaiSensorBridge` in `iosApp/iosApp/Akamai/` conforms to that protocol; the implementation is wired into the DI graph via `ProdAppGraph.Factory` from `IosApp`.

## Public API (api module)

| Type | Description |
|------|-------------|
| `HttpClientFactory` | `fun interface` — creates per-feature `HttpClient` instances via DSL config |
| `ClientConfig` | DSL class — `baseUrl`, `authMode`, `requestTimeout`, `connectTimeout`, `socketTimeout`, `headers`, `ktorConfig` escape hatch |
| `AuthMode` | Enum — `BEARER` (default) or `NONE` (public endpoints) |
| `SensorDataProvider` | `suspend fun currentSensorData(): String` — supplies the Akamai Bot Manager token the factory attaches to every request |
| `AkamaiSensorBridge` (iosMain) | Kotlin contract for the iOS Akamai SDK, mirroring `HarnessIosBridge`. Implemented in Swift (`SwiftAkamaiSensorBridge`); supplied to DI via `ProdAppGraph.Factory`. Exported to Swift as `ApiAkamaiSensorBridge` (api-module prefix). |
| `NetworkException` | Sealed class — `HttpError`, `Timeout`, `NoConnectivity`, `Serialization`, `Unknown` |

## Implementation (impl module)

| Type | Description |
|------|-------------|
| `HttpClientFactoryImpl` | `@SingleIn(AppScope)` `@ContributesBinding` factory. Takes a shared base `HttpClient` whose engine is re-used by every derived per-feature client (via `baseClient.config { }`). Bakes in: JSON content negotiation, `X-App-Id` / `X-Market` / `User-Agent` headers, dev logging (non-prod), `HttpTimeout`, `HttpCookies`, `HttpRequestRetry` (502/503/504 + 429, idempotent methods only or `Idempotency-Key` opt-in, exponential with `Retry-After`), Akamai sensor-data header, Akamai debug `Pragma` headers (non-prod), and — when `AuthMode.BEARER` — Ktor's `Auth` plugin host-pinned to the client's `baseUrl`. Features configure via DSL. |
| `HttpClientProvider` | `@ContributesTo` interface providing the singleton base `HttpClient` (default engine per platform) |
| `JsonProvider` | `@ContributesTo` interface providing singleton `Json` instance with `ignoreUnknownKeys`, `isLenient`, `encodeDefaults`, `explicitNulls = false` |
| `AkamaiSensorDataProviderAndroid` | `androidMain` `@ContributesBinding` — takes `Application`. POC default returns empty; swap the body for a call into the Akamai Bot Manager Android SDK once the AAR is dropped into `core/network/impl/libs/`. |
| `AkamaiSensorDataProviderIos` | `iosMain` `@ContributesBinding` — delegates `currentSensorData()` to the `AkamaiSensorBridge` (defined in `core:network:api`). |

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
- **User-Agent** — explicit agent string derived from `AppBuildConfig` (`appName/env-market (buildType)`); never uses Ktor's engine-default UA (Akamai Bot Manager keys off UA shape)
- **HttpTimeout** — defaults: request 15s, connect 5s, socket 10s (overridable via DSL)
- **HttpCookies** — per-client cookie jar; `Set-Cookie` responses are preserved and replayed on subsequent requests on the same client. Required so Akamai session cookies (`ak_bmsc`, `AKA_A2`) stay pinned to the same edge node across retries.
- **HttpRequestRetry** — retries up to 3 times, exponential backoff, `Retry-After` respected. Status gate: `502`, `503`, `504`, or `429` (500/501/505 are not retried — non-transient or ambiguous about whether a write succeeded). Method gate: only idempotent methods (`GET`, `HEAD`, `OPTIONS`, `PUT`, `DELETE`) retry automatically. `POST`/`PATCH` retry only when the caller sets an `Idempotency-Key` header so the backend can dedupe. Same gate applies to network-exception retries (`retryOnExceptionIf`). RFC 9110 §9.2.2.
- **Akamai sensor data** — `SensorDataProvider.currentSensorData()` is called per request; when non-empty, attached as the `X-acf-sensor-data` header. Android binding takes `Application` and calls the Akamai Bot Manager SDK once wired; iOS binding delegates to a Swift bridge. Both return empty by default so the header is omitted until the real SDK is in place.
- **Akamai debug Pragma headers** — `akamai-x-cache-on`, `akamai-x-cache-remote-on`, `akamai-x-get-true-cache-key`, `akamai-x-get-cache-key` added in non-prod (env ≠ `prod`) for edge troubleshooting; omitted in prod.
- **Logging** — `LogLevel.HEADERS` in non-prod, `LogLevel.NONE` in prod
- **Auth (bearer)** — installed when `AuthMode.BEARER`. `loadTokens`/`refreshTokens` delegate to `AuthManager`; cross-client refresh coordination lives in `AuthManager` (one refresh call for concurrent 401 bursts across clients). Tokens are host-pinned to the client's `baseUrl` host **and** protocol — `sendWithoutRequest` only attaches the `Authorization` header when the request URL matches, and `refreshTokens` returns null for non-matching hosts (closes the reactive 401→refresh leak path on absolute-URL or redirected requests). `AuthMode.BEARER` therefore requires `baseUrl` to be set; `create { }` throws `IllegalStateException` otherwise. Skipped when `AuthMode.NONE`.
- **Cache-header pass-through** — Ktor does not mutate `Cache-Control`, `ETag`, `If-None-Match`, or `If-Modified-Since` on outbound requests; features using conditional GETs can set these directly via `HttpRequestBuilder.headers`.

## Shared Engine

The factory holds one `HttpClient` as its base. Each `create()` call returns a derived client via `baseClient.config { }`, which shares the underlying engine (connection pool, TLS config) with the base. Per-feature clients remain isolated for plugin/header/timeout configuration but do not duplicate engine resources.

## Rules

- Core modules never import from features
- Only `impl/data` modules should depend on `core:network:api` (plus `core:network:impl` itself)
- `composeApp` depends on `core:network:impl` (DI graph wiring)
- Presenter and UI modules must NEVER import or use `HttpClient` directly
- All network calls must go through a repository → data source, consumed via `StrataInteractor` in the presenter layer
- Each feature creates its own client — no shared singleton `HttpClient`
- Ktor engine is resolved automatically per platform (OkHttp on Android, Darwin on iOS)
