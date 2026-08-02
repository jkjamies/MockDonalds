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

## Pre-flight: Grill the Spec

When the user provides a spec via `@file`, scan it for unresolved markers before scaffolding (see the grill-me skill for the full marker list): `<!-- TODO -->` placeholders, empty `- [ ]` AC items, empty required header fields, raw template placeholder prose, `...` table cells, or unconfirmed reverse-spec presumptions (`presumably` / `appears to`). If any are present, **stop and run `/grill-me @{spec}` first**, then resume this skill.

The conversion skills (`/ac-to-spec`, `/reverse-spec`) grill inline before producing output, so a marker-laden spec usually means the spec was hand-authored from a template or has gone stale. Skip the pre-flight only if the user explicitly says "skip the grill" — in that case, surface unresolved markers as `// TODO` comments in the generated code and call them out in the final summary.

## Pre-flight: Subagent dispatch (greenfield convention survey)

Even for new features, dispatch one `Explore` agent BEFORE step 1 below to map the existing patterns the new feature must match. This catches drift between the scaffold and current conventions (recent test-tag formats, new `core:` modules, updated convention plugins, the most-recently-added feature's AGENTS.md shape). New features land 6+ modules and 9+ files — exactly the surface where pre-flight pays off.

**Recommended prompt template:**

```
Survey existing features as the canonical reference for scaffolding a new
feature `{name}` with primary screen `{Screen}`. Report:
- Full file structure of `features/order/` (use as the canonical reference) —
  every file and a 1-line purpose.
- Convention plugin assignments — which plugin each impl/* module declares.
- The most-recently-added feature's AGENTS.md shape — sections in order.
- Any `.agents/standards/*.md` files updated in the last 30 days that affect
  feature scaffolding (specifically: feature-scaffolding.md,
  naming-conventions.md, dependency-injection.md).
Report findings as a checklist of what the new feature must match.
```

See `.agents/standards/ways-of-working.md` → "When to Spawn Subagents". Skip only if the feature is an intentional one-file proof-of-concept.

## Reference Standards

- Architecture & module structure: `.agents/standards/architecture.md`
- Feature scaffolding guide: `.agents/standards/feature-scaffolding.md`
- Naming conventions: `.agents/standards/naming-conventions.md`
- DI patterns: `.agents/standards/dependency-injection.md`
- Testing conventions: `.agents/standards/testing.md` (overview)
- Unit tests: `.agents/standards/testing-unit.md`
- UI component tests: `.agents/standards/testing-ui-component.md`
- Strata interactors: `.agents/standards/strata.md`
- Design system & adaptive layout: `.agents/standards/design-system.md`
- Convention plugins: `.agents/standards/convention-plugins.md`

## Reference Implementation

Use `features/order/` as the reference for file patterns, naming, and structure.

## Steps

### 1. Create Module Directories and Build Files

Create 6 submodules under `features/{name}/`:

**api/domain/build.gradle.kts** — public models and use case abstractions:
```kotlin
plugins { id("sampleplatter.kmp.library") }
```

**api/navigation/build.gradle.kts** — Screen objects and TestTags:
```kotlin
plugins { id("sampleplatter.kmp.library") }
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
plugins { id("sampleplatter.kmp.domain") }
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
plugins { id("sampleplatter.kmp.data") }
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:{name}:impl:domain"))
            implementation(project(":core:network:api"))
            implementation(project(":core:build-config:api"))
        }
    }
}
```

**impl/presentation/build.gradle.kts** — presenter, UI, state:
```kotlin
plugins { id("sampleplatter.kmp.presentation") }
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
plugins { id("sampleplatter.kmp.domain") }
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

**api/domain/** — `src/commonMain/kotlin/com/jkjamies/sampleplatter/features/{name}/api/domain/`

`{Feature}Content.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.api.domain

import kotlinx.serialization.Serializable

@Serializable
data class {Feature}Content(
    // domain model fields
)
```

`Get{Feature}Content.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.api.domain

import com.jkjamies.sampleplatter.core.strata.StrataSubjectInteractor

abstract class Get{Feature}Content : StrataSubjectInteractor<Unit, {Feature}Content>()
```

**api/navigation/** — `src/commonMain/kotlin/com/jkjamies/sampleplatter/features/{name}/api/navigation/`

`{Feature}Screen.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.api.navigation

import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data object {Feature}Screen : Screen
```

For tab screens, use `TabScreen` instead of `Screen` and add `override val tag: String = "{name}"`.
For auth-gated screens, use `ProtectedScreen` instead of `Screen`.

`{Feature}TestTags.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.api.ui

object {Feature}TestTags {
    const val SCREEN = "{name}_screen"
}
```

**impl/domain/** — `src/commonMain/kotlin/com/jkjamies/sampleplatter/features/{name}/domain/`

`Get{Feature}ContentImpl.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.domain

import com.jkjamies.sampleplatter.features.{name}.api.domain.Get{Feature}Content
import com.jkjamies.sampleplatter.features.{name}.api.domain.{Feature}Content
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
package com.jkjamies.sampleplatter.features.{name}.domain

import com.jkjamies.sampleplatter.features.{name}.api.domain.{Feature}Content
import kotlinx.coroutines.flow.Flow

interface {Feature}Repository {
    fun get{Feature}(): Flow<{Feature}Content>
}
```

**impl/data/** — `src/commonMain/kotlin/com/jkjamies/sampleplatter/features/{name}/data/`

`{Feature}RepositoryImpl.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.data

import com.jkjamies.sampleplatter.features.{name}.api.domain.{Feature}Content
import com.jkjamies.sampleplatter.features.{name}.data.remote.{Feature}RemoteDataSource
import com.jkjamies.sampleplatter.features.{name}.domain.{Feature}Repository
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

**impl/data/remote/** — `src/commonMain/kotlin/com/jkjamies/sampleplatter/features/{name}/data/remote/`

`{Feature}RemoteDataSource.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.data.remote

import kotlinx.coroutines.flow.Flow

interface {Feature}RemoteDataSource {
    fun get{Feature}(): Flow<{Feature}Dto>
}
```

`{Feature}RemoteDataSourceImpl.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.data.remote

import com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfig
import com.jkjamies.sampleplatter.core.network.HttpClientFactory
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
package com.jkjamies.sampleplatter.features.{name}.data.remote

import com.jkjamies.sampleplatter.features.{name}.api.domain.{Feature}Content
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

The presenter collects via `collectContentAsState()`, not `collectAsState()` — the latter collapses loading, empty, and failed into a single `null` and lets stream exceptions reach composition. `{Feature}UiState` therefore carries `isLoading: Boolean = false` and `errorMessage: String? = null` alongside its content fields, and `{Feature}Ui.kt` renders all three states. Most existing screens predate this and still use `collectAsState()` — follow `features/order/` for structure, but use the shape below for state collection. See `.agents/standards/strata.md` → "Two collection surfaces".

```kotlin
val content by get{Feature}Content.collectContentAsState()

return {Feature}UiState(
    // content fields via content.dataOrNull?.…
    isLoading = content.isLoading,
    errorMessage = content.errorOrNull?.message,
    eventSink = { event -> … },
)
```

`{Feature}Ui.kt` must expose a composable named exactly after the file (`{Feature}Ui`) carrying `@CircuitInject({Feature}Screen::class, AppScope::class)`. Any helper composables in the same file must be `private` — `composeApp` consumes presentation via `api(project(...))`, so a public helper becomes app-wide API surface. Both are enforced by `UiCompositionConventionsTest`.

### 2b. Create the iOS View — NOT OPTIONAL

**A feature without a SwiftUI view is an Android-only feature.** The Kotlin side compiles, every Konsist rule passes, and iOS renders `Text("No UI for screen: …")` — `Circuit.swift` falls back rather than failing, so this is silent at runtime. `PlatformParityTest` fails the build if you skip this step.

`iosApp/iosApp/Features/{Feature}/{Feature}View.swift`:

```swift
import SwiftUI
import ComposeApp
import CircuitMacros

private let tags = {Feature}TestTags.shared

@CircuitInject({Feature}Screen.self, {Feature}UiState.self)
struct {Feature}View: View {
    let state: {Feature}UiState

    var body: some View {
        // Render from `state`; send events via `state.eventSink(...)`.
        // Tag every asserted element with `.accessibilityIdentifier(tags.SCREEN)` etc.
    }
}
```

Rules (all Harmonize-enforced by `ViewConventionsTest`):
- The struct name must match the file name, conform to `View`, and hold a `state` property.
- `import ComposeApp` is required; `import UIKit` is banned (pure SwiftUI).
- No force unwrap / force cast / force try, no Combine or `DispatchQueue` (async/await only), no `print`, no TODO/FIXME/HACK.
- Use `accessibilityIdentifier` with the shared `{Feature}TestTags` from KMP — never a hardcoded string.
- `@CircuitInject` drives `CircuitFactoryRegistry` codegen, so `AppDelegate.swift` never needs editing.

Also create the iOS UI component tests in `iosApp/iosAppTests/UIComponent/{Feature}/`: `{Feature}ViewTest.swift`, `{Feature}ViewRobot.swift`, `{Feature}StateRobot.swift`. See `add-screen` → "Files to Create" and `.agents/standards/testing-ui-component.md`.

### 2c. Register the screen at runtime

Module discovery is automatic (`settings.gradle.kts` walks `features/`), but **runtime registration is not**. Skip these and the feature builds, ships, and is unreachable:

| If the feature… | Edit | Change |
|---|---|---|
| is a bottom-nav tab | `composeApp/.../navigation/DeepLinkParser.kt` | add the screen to `tabScreens` |
| should be deep-linkable | same file, `createDeepLinkParser()` | add `"{path}" to { {Feature}Screen }` |
| is a tab | `composeApp/src/androidMain/.../SamplePlatterBottomNavigation.kt` | add the nav item |
| is reachable from More | `features/{name}/impl/presentation/` | contribute a `MoreTabExtension` via `@ContributesIntoSet` (see `features/debug-menu/.../DebugMenuTabExtension.kt`) |

A `TabScreen` also needs `override val tag: String = "{name}"`, and the tag must match the deep-link path segment.

**test/** — `src/commonMain/kotlin/com/jkjamies/sampleplatter/features/{name}/test/`

`FakeGet{Feature}Content.kt`:
```kotlin
package com.jkjamies.sampleplatter.features.{name}.test

import com.jkjamies.sampleplatter.features.{name}.api.domain.Get{Feature}Content
import com.jkjamies.sampleplatter.features.{name}.api.domain.{Feature}Content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGet{Feature}Content(
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
- `iosApp/iosAppTests/UIComponent/{Feature}/` — `{Feature}ViewTest.swift`, `{Feature}ViewRobot.swift`, `{Feature}StateRobot.swift`

### 4. Create Feature AGENTS.md

Create `features/{name}/AGENTS.md` following the template in the root AGENTS.md plan. Include business context, key types table, cross-feature dependencies, and testing paths.

### 5. Verify Auto-Discovery

The feature should be auto-discovered by `settings.gradle.kts`. Verify:
```bash
./gradlew projects | grep {name}
```

Should show all 6 submodules. If not, check that the directory name matches the feature loop in `settings.gradle.kts`.

The feature is also auto-exported to the iOS framework by `composeApp/build.gradle.kts` (`api:domain`, `api:navigation`, `impl:presentation`). Keep the public surface of those three modules to what SwiftUI actually names — every exported declaration is a dead-code-elimination root in a static framework, so anything public there is permanently in the iOS binary.

### 6. Update the docs in the same change

`AgentDocumentationDriftTest` fails if the feature is not named in the entry-point docs, because an agent routing off `AGENTS.md` will not find it:

- root `AGENTS.md` → "Features:" list
- `README.md` → "Features:" list and the feature table

### 7. Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** For new features, run `verify full` (not `verify diff`) since scaffolding touches many modules and requires a full build to validate wiring.

The `verify` skill will run: lint, unit tests, architecture tests (Konsist + Harmonize), and full build.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
