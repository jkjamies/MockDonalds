---
name: add-api-endpoint
description: "Add a new API endpoint to a feature — data source interface, implementation, DTO, mapper, and HttpClient config. Use when connecting a feature to a backend service. NOTE: core:network infrastructure exists but not all features have real API integrations yet — this skill scaffolds the wiring; actual endpoint details depend on backend readiness."
---

# Add API Endpoint

> **Infrastructure status**: `core:network` provides `HttpClientFactory` with JSON, auth, headers, logging, and timeouts. Features create per-feature `HttpClient` instances. Not all features have real API integrations yet — some use in-memory data. This skill scaffolds the full data layer wiring; use `/migrate` to convert existing in-memory repos to real API calls.

Wire a feature to a backend API endpoint with all required data layer files.

**Parameters**: feature name, endpoint description (optional if spec provides them)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — feature name + endpoint description, e.g., `/add-api-endpoint order fetch cart items`.
2. **`@file` reference** — e.g., `/add-api-endpoint @specs/deals-api.md`. The CLI resolves the file and includes its content. Extract endpoint path, method, auth mode, request/response shapes, and error handling from the spec. Template: `.agents/templates/new-spec.md` (API / Network section).
3. **Inline description** — free text with endpoint details.

## Reference Standards

- Network patterns: `.agents/standards/architecture.md` (data layer section)
- DI patterns: `.agents/standards/dependency-injection.md`
- Naming conventions: `.agents/standards/naming-conventions.md`

## Reference Implementation

- `core/network/api/` — `HttpClientFactory`, `ClientConfig`, `AuthMode`
- `core/network/AGENTS.md` — full network layer documentation

## Files to Create / Modify

### 1. Remote Data Source Interface — `impl/data/remote/`

`features/{feature}/impl/data/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/data/remote/{Name}RemoteDataSource.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data.remote

import kotlinx.coroutines.flow.Flow

interface {Name}RemoteDataSource {
    fun get{Name}(): Flow<{Name}Dto>
    // or: suspend fun submit{Action}(request: {Request}Dto): {Response}Dto
}
```

### 2. Remote Data Source Implementation — `impl/data/remote/`

`features/{feature}/impl/data/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/data/remote/{Name}RemoteDataSourceImpl.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data.remote

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.core.network.HttpClientFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@ContributesBinding(AppScope::class)
@Inject
class {Name}RemoteDataSourceImpl(
    httpClientFactory: HttpClientFactory,
    appBuildConfig: AppBuildConfig,
) : {Name}RemoteDataSource {

    private val client: HttpClient = httpClientFactory.create {
        baseUrl = appBuildConfig.{service}BaseUrl
        // authMode = AuthMode.BEARER  (default)
        // requestTimeout = 10.seconds (override if needed)
    }

    override fun get{Name}(): Flow<{Name}Dto> = flow {
        emit(client.get("{endpoint}").body<{Name}Dto>())
    }
}
```

**HttpClientFactory configuration options** (from `ClientConfig`):
| Option | Default | Description |
|--------|---------|-------------|
| `baseUrl` | none | Required — use `appBuildConfig.{service}BaseUrl` |
| `authMode` | `AuthMode.BEARER` | `BEARER` (auto-refresh on 401) or `NONE` |
| `requestTimeout` | `15.seconds` | Total request timeout |
| `connectTimeout` | `5.seconds` | TCP connection timeout |
| `socketTimeout` | `10.seconds` | Socket read timeout |
| `header(name, value)` | — | Add custom headers |
| `ktorConfig { }` | — | Escape hatch for raw Ktor config |

### 3. DTO — `impl/data/remote/`

`features/{feature}/impl/data/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/data/remote/{Name}Dto.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class {Name}Dto(
    @SerialName("field_name") val fieldName: String,
    // fields matching API JSON response
)
```

### 4. Mapper — extension function in DTO file or separate file

```kotlin
fun {Name}Dto.to{DomainModel}(): {DomainModel} = {DomainModel}(
    fieldName = fieldName,
    // map DTO fields to domain model
)
```

### 5. Update Repository Implementation

Update `features/{feature}/impl/data/.../{ Feature}RepositoryImpl.kt` to inject and use the new data source:

```kotlin
@ContributesBinding(AppScope::class)
@Inject
class {Feature}RepositoryImpl(
    private val remoteDataSource: {Name}RemoteDataSource,
) : {Feature}Repository {
    override fun get{Feature}(): Flow<{DomainModel}> =
        remoteDataSource.get{Name}().map { it.to{DomainModel}() }
}
```

### 6. Update Build File (if needed)

Ensure `impl/data/build.gradle.kts` has:
```kotlin
implementation(project(":core:network:api"))
implementation(project(":core:build-config"))
```

### 7. Build Config (if new base URL needed)

If the endpoint requires a new base URL not already in `AppBuildConfig`, use `/add-config-field` first to add it.

## DTO Conventions (Konsist-enforced)

- `@Serializable` annotation required
- Class name must end with `Dto`
- Must live in `remote/` package within `impl/data`
- Use `@SerialName` for snake_case JSON fields mapping to camelCase Kotlin
- Nullable fields for optional API response fields

## Key Rules

- **One HttpClient per feature** — never share clients across features
- **Data sources are interface + Impl** — Konsist enforces this pattern
- **DTOs never leak to domain** — always map via extension functions
- **Only `impl/data` depends on `core:network:api`** — Konsist enforces this
- **Presenters never touch repositories** — only through CenterPost interactors

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify` skill to validate all changes.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
