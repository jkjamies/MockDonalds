---
name: add-repository
description: Add a new repository with interface, implementation, and test. Use when adding a new data source to a feature.
---

# Add Repository

Create a repository interface, implementation, and test.

**Parameters**: feature name, repository name

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — just the feature and repository name. Scaffold with placeholders.
2. **`@file` reference** — e.g., `/add-repository @specs/payment-repo.md`. The CLI resolves the file and includes its content. Use it to populate repository methods, data types, DTO fields, endpoint paths, mapper logic, and test assertions instead of using placeholders. If no arguments are provided, extract feature and repository name from the spec's Overview / Repository section.
3. **Inline description** — free text typed after the parameters (or on its own). Extract whatever is provided (feature name, repository name, methods, return types, endpoint details, DTO shape) and use it the same way as a spec file.

When context is provided, replace placeholders with real values everywhere: interface methods, impl logic, DTOs, mappers, data source methods, and test cases. If context is partial, fill in what you can and leave `// TODO` only for genuinely unknown parts.

Templates are available in `.agents/templates/new-spec.md` for structured input.

## Reference Standards

- DI patterns: `.agents/standards/dependency-injection.md`

## Reference

- Interface: `features/order/impl/domain/src/commonMain/.../OrderRepository.kt`
- Impl: `features/order/impl/data/src/commonMain/.../OrderRepositoryImpl.kt`
- Test: `features/order/impl/data/src/commonTest/.../OrderRepositoryImplTest.kt`

## Files to Create

### 1. Interface — `impl/domain/`

`features/{feature}/impl/domain/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/domain/{Name}Repository.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.domain

import kotlinx.coroutines.flow.Flow

interface {Name}Repository {
    fun getData(): Flow<{DataType}>
}
```

Repository functions should return `Flow<T>` for streaming data. Use `suspend fun` only for one-shot operations (rare).

### 2. Implementation — `impl/data/`

`features/{feature}/impl/data/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/data/{Name}RepositoryImpl.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data

import com.mockdonalds.app.features.{feature}.domain.{Name}Repository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class {Name}RepositoryImpl : {Name}Repository {
    override fun getData(): Flow<{DataType}> = flowOf(
        // implementation
    )
}
```

### 3. Test — `impl/data/commonTest/`

`features/{feature}/impl/data/src/commonTest/kotlin/com/mockdonalds/app/features/{feature}/data/{Name}RepositoryImplTest.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class {Name}RepositoryImplTest : BehaviorSpec({

    Given("a {Name}RepositoryImpl") {
        val repository = {Name}RepositoryImpl()

        When("fetching data") {
            Then("it should return expected values") {
                val result = repository.getData().first()
                // assertions
            }
        }
    }
})
```

### 4. Remote Data Source (if network-backed) — `impl/data/remote/`

`features/{feature}/impl/data/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/data/remote/{Name}RemoteDataSource.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data.remote

import kotlinx.coroutines.flow.Flow

interface {Name}RemoteDataSource {
    fun getData(): Flow<{Name}Dto>
}
```

`features/{feature}/impl/data/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/data/remote/{Name}RemoteDataSourceImpl.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data.remote

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.core.network.HttpClientFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@ContributesBinding(AppScope::class)
@Inject
class {Name}RemoteDataSourceImpl(
    httpClientFactory: HttpClientFactory,
    appBuildConfig: AppBuildConfig,
) : {Name}RemoteDataSource {

    private val client: HttpClient = httpClientFactory.create {
        baseUrl = appBuildConfig.{service}BaseUrl  // e.g., menuBaseUrl, orderBaseUrl — see AppBuildConfig
    }

    override fun getData(): Flow<{Name}Dto> = flow {
        // client.get("{endpoint}").body<{Name}Dto>()
    }
}
```

### 5. DTO (if network-backed) — `impl/data/remote/`

`features/{feature}/impl/data/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/data/remote/{Name}Dto.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class {Name}Dto(
    // fields matching API response
)
```

DTOs must be `@Serializable` data classes with `Dto` suffix, located in `remote/` package (Konsist enforces this).

## Key Rules

- Interface in `impl/domain/` — visible to use cases in the same feature
- Impl in `impl/data/` — private, must have `@ContributesBinding(AppScope::class)`
- Impl name must end with `RepositoryImpl` (Konsist enforces this)
- Impl must implement the interface (Konsist enforces this)
- Presenters must NOT depend on repositories directly — only through use cases
- Remote data sources go in `impl/data/remote/`, local in `impl/data/local/` (Konsist enforces this)
- DTOs must be `@Serializable` data classes with `Dto` suffix in `remote/` (Konsist enforces this)
- Only `impl/data` modules may depend on `core:network:api` (Konsist enforces this)
- Each feature creates its own `HttpClient` via `HttpClientFactory` — no shared singleton

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes. It will:

- Detect which modules were affected by the new repository files
- Run lint, unit tests, and architecture checks scoped to those modules
- Catch naming violations, missing `@ContributesBinding`, layer isolation issues

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
