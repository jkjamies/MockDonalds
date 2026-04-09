# core:network

## Purpose

Provides a pre-configured Ktor `HttpClient` for network operations.
Sets up JSON content negotiation with sensible defaults (lenient parsing,
unknown keys ignored).

## Public API

| Type | Description |
|------|-------------|
| `createHttpClient()` | Top-level function returning a configured `HttpClient` with `ContentNegotiation` plugin and `kotlinx.serialization.json.Json` (lenient, ignores unknown keys). |

## Usage

Consumed by `impl` / data-layer modules that need HTTP access:

```kotlin
class MenuRepositoryImpl @Inject constructor() : MenuRepository {
    private val client = createHttpClient()

    override suspend fun getMenu(): List<MenuItem> {
        return client.get("https://api.example.com/menu").body()
    }
}
```

The Ktor engine is resolved automatically per platform (OkHttp on Android,
Darwin on iOS) via Gradle engine dependencies.

## Rules

- Core modules never import from features
- Only `impl` and data-layer modules should depend on `core:network`
- Presenter and UI modules must NEVER import or use `HttpClient` directly
- All network calls must go through a repository, which is consumed via
  a `CenterPostInteractor` in the presenter layer
- Do not create additional `HttpClient` instances -- use `createHttpClient()`
