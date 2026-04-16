---
name: add-feature
description: Scaffold a complete new feature module with all 6 submodules, source files, tests, fakes, and AGENTS.md. Use when adding an entirely new feature to the app.
---

# Add Feature

Scaffold a complete feature with all layers, tests, and documentation.

**Parameters**: feature name (lowercase, e.g., `deals`), primary screen name (PascalCase, e.g., `Deals`)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — just the feature name. Scaffold with placeholders (`// TODO` comments, placeholder fields).
2. **`@file` reference** — e.g., `/add-feature @specs/deals.md`. The CLI resolves the file and includes its content. Use it to populate domain model fields, screen types, endpoint paths, DTO shapes, test assertions, and AGENTS.md business context instead of using placeholders. If no feature name is provided as an argument, extract it from the spec's **Name** field in the Overview section.
3. **Inline description** — free text typed after the feature name (or on its own). Extract whatever is provided (feature name, field names, screen type, API details, business rules) and use it the same way as a spec file.

When context is provided, replace placeholders with real values everywhere: domain models, DTOs, mappers, presenter state, UI composables, test defaults, and fakes. If context is partial (e.g., field names but no endpoint), fill in what you can and leave `// TODO` only for genuinely unknown parts.

Templates are available in `.agents/templates/new-spec.md` for structured input.

## Reference Standards

- Architecture & module structure: `.agents/standards/architecture.md`
- Feature scaffolding guide: `.agents/standards/feature-scaffolding.md`
- Naming conventions: `.agents/standards/naming-conventions.md`
- DI patterns: `.agents/standards/dependency-injection.md`
- Testing conventions: `.agents/standards/testing.md` (overview)
- Unit tests: `.agents/standards/testing-unit.md`
- UI component tests: `.agents/standards/testing-ui-component.md`
- CenterPost interactors: `.agents/standards/centerpost.md`
- Design system & adaptive layout: `.agents/standards/design-system.md`
- Convention plugins: `.agents/standards/convention-plugins.md`

## Reference Implementation

Use `features/order/` as the reference for file patterns, naming, and structure.

## Steps

### 1. Create Module Directories and Build Files

Create 6 submodules under `features/{name}/`:

**api/domain/build.gradle.kts** — public models and use case abstractions:
```kotlin
plugins { id("mockdonalds.kmp.library") }
```

**api/navigation/build.gradle.kts** — Screen objects and TestTags:
```kotlin
plugins { id("mockdonalds.kmp.library") }
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:circuit"))
        }
    }
}
```

**impl/domain/build.gradle.kts** — use case implementations:
```kotlin
plugins { id("mockdonalds.kmp.domain") }
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:{name}:api:domain"))
        }
    }
}
```

**impl/data/build.gradle.kts** — repository implementations, data sources, DTOs:
```kotlin
plugins { id("mockdonalds.kmp.data") }
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:{name}:impl:domain"))
            implementation(project(":core:network:api"))
            implementation(project(":core:build-config"))
        }
    }
}
```

**impl/presentation/build.gradle.kts** — presenter, UI, state:
```kotlin
plugins { id("mockdonalds.kmp.presentation") }
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:{name}:api:domain"))
            implementation(project(":features:{name}:api:navigation"))
        }
    }
}
```

**test/build.gradle.kts** — fakes for testing:
```kotlin
plugins { id("mockdonalds.kmp.domain") }
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":features:{name}:api:domain"))
            api(project(":core:test-fixtures"))
        }
    }
}
```

### 2. Create Source Files

**api/domain/** — `src/commonMain/kotlin/com/mockdonalds/app/features/{name}/api/domain/`

`{Feature}Content.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.api.domain

import kotlinx.serialization.Serializable

@Serializable
data class {Feature}Content(
    // domain model fields
)
```

`Get{Feature}Content.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.api.domain

import com.mockdonalds.app.core.centerpost.CenterPostSubjectInteractor

abstract class Get{Feature}Content : CenterPostSubjectInteractor<Unit, {Feature}Content>()
```

**api/navigation/** — `src/commonMain/kotlin/com/mockdonalds/app/features/{name}/api/navigation/`

`{Feature}Screen.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data object {Feature}Screen : Screen
```

For tab screens, use `TabScreen` instead of `Screen` and add `override val tag: String = "{name}"`.
For auth-gated screens, use `ProtectedScreen` instead of `Screen`.

`{Feature}TestTags.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.api.ui

object {Feature}TestTags {
    const val SCREEN = "{name}_screen"
}
```

**impl/domain/** — `src/commonMain/kotlin/com/mockdonalds/app/features/{name}/domain/`

`Get{Feature}ContentImpl.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.domain

import com.mockdonalds.app.features.{name}.api.domain.Get{Feature}Content
import com.mockdonalds.app.features.{name}.api.domain.{Feature}Content
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class Get{Feature}ContentImpl(
    private val repository: {Feature}Repository,
) : Get{Feature}Content() {
    override fun createObservable(params: Unit): Flow<{Feature}Content> {
        return repository.get{Feature}()
    }
}
```

`{Feature}Repository.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.domain

import com.mockdonalds.app.features.{name}.api.domain.{Feature}Content
import kotlinx.coroutines.flow.Flow

interface {Feature}Repository {
    fun get{Feature}(): Flow<{Feature}Content>
}
```

**impl/data/** — `src/commonMain/kotlin/com/mockdonalds/app/features/{name}/data/`

`{Feature}RepositoryImpl.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.data

import com.mockdonalds.app.features.{name}.api.domain.{Feature}Content
import com.mockdonalds.app.features.{name}.data.remote.{Feature}RemoteDataSource
import com.mockdonalds.app.features.{name}.domain.{Feature}Repository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
@Inject
class {Feature}RepositoryImpl(
    private val remoteDataSource: {Feature}RemoteDataSource,
) : {Feature}Repository {
    override fun get{Feature}(): Flow<{Feature}Content> =
        remoteDataSource.get{Feature}().map { it.toContent() }
}
```

**impl/data/remote/** — `src/commonMain/kotlin/com/mockdonalds/app/features/{name}/data/remote/`

`{Feature}RemoteDataSource.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.data.remote

import kotlinx.coroutines.flow.Flow

interface {Feature}RemoteDataSource {
    fun get{Feature}(): Flow<{Feature}Dto>
}
```

`{Feature}RemoteDataSourceImpl.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.data.remote

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
class {Feature}RemoteDataSourceImpl(
    httpClientFactory: HttpClientFactory,
    appBuildConfig: AppBuildConfig,
) : {Feature}RemoteDataSource {

    private val client: HttpClient = httpClientFactory.create {
        baseUrl = appBuildConfig.{service}BaseUrl  // e.g., menuBaseUrl, orderBaseUrl — see AppBuildConfig
    }

    override fun get{Feature}(): Flow<{Feature}Dto> = flow {
        // client.get("{endpoint}").body<{Feature}Dto>()
    }
}
```

`{Feature}Dto.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.data.remote

import com.mockdonalds.app.features.{name}.api.domain.{Feature}Content
import kotlinx.serialization.Serializable

@Serializable
data class {Feature}Dto(
    // fields matching API response
)

fun {Feature}Dto.toContent(): {Feature}Content = {Feature}Content(
    // map DTO fields to domain model
)
```

**impl/presentation/** — see reference files in `features/order/impl/presentation/`

Create: `{Feature}Presenter.kt`, `{Feature}UiState.kt` (with sealed class `{Feature}Event`), `{Feature}Ui.kt` (in androidMain).

**test/** — `src/commonMain/kotlin/com/mockdonalds/app/features/{name}/test/`

`FakeGet{Feature}Content.kt`:
```kotlin
package com.mockdonalds.app.features.{name}.test

import com.mockdonalds.app.features.{name}.api.domain.Get{Feature}Content
import com.mockdonalds.app.features.{name}.api.domain.{Feature}Content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGet{Feature}Content @Inject constructor(
    initial: {Feature}Content = DEFAULT,
) : Get{Feature}Content() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<{Feature}Content> = _content

    fun emit(content: {Feature}Content) {
        _content.value = content
    }

    companion object {
        val DEFAULT = {Feature}Content(/* test defaults */)
    }
}
```

### 3. Create Test Files

Follow `add-unit-tests` and `add-ui-tests` skills for templates:
- `impl/domain/src/commonTest/` — `Get{Feature}ContentImplTest.kt`
- `impl/data/src/commonTest/` — `{Feature}RepositoryImplTest.kt`
- `impl/presentation/src/commonTest/` — `{Feature}PresenterTest.kt`
- `impl/presentation/src/androidDeviceTest/` — `{Feature}UiTest.kt`, `{Feature}UiRobot.kt`, `{Feature}StateRobot.kt`, `AndroidManifest.xml`

### 4. Create Feature AGENTS.md

Create `features/{name}/AGENTS.md` following the template in the root AGENTS.md plan. Include business context, key types table, cross-feature dependencies, and testing paths.

### 5. Verify Auto-Discovery

The feature should be auto-discovered by `settings.gradle.kts`. Verify:
```bash
./gradlew projects | grep {name}
```

Should show all 6 submodules. If not, check that the directory name matches the feature loop in `settings.gradle.kts`.

### 6. Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** For new features, run `verify full` (not `verify diff`) since scaffolding touches many modules and requires a full build to validate wiring.

The `verify` skill will run: lint, unit tests, architecture tests (Konsist + Harmonize), and full build.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
