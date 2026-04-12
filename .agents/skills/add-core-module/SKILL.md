---
name: add-core-module
description: Scaffold a new core module with api/impl split, optional test module, source files, and AGENTS.md. Use when adding shared infrastructure to the app.
---

# Add Core Module

Scaffold a core module with api/impl split for shared infrastructure.

**Parameters**:
- `name` (lowercase kebab-case, e.g., `feature-flag`) — required
- `hasTest` (boolean, default `true`) — whether to create a `test/` submodule with fakes
- `plugin` (`mockdonalds.kmp.domain` for modules needing Metro DI, `mockdonalds.kmp.library` for pure contracts) — default `mockdonalds.kmp.domain`

## Reference Standards

- Architecture & module structure: `.agents/standards/architecture.md`
- Naming conventions: `.agents/standards/naming-conventions.md`
- DI patterns: `.agents/standards/dependency-injection.md`
- CenterPost interactors: `.agents/standards/centerpost.md`
- Convention plugins: `.agents/standards/convention-plugins.md`

## Reference Implementation

Use `core/auth/` as the reference for api/impl split pattern, DI wiring, and AGENTS.md format.

## Steps

### 1. Create Module Directories and Build Files

Create submodules under `core/{name}/`:

**api/build.gradle.kts** — public contract (interfaces, abstract interactors, types):
```kotlin
plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.{name}.api"
    }

    sourceSets {
        commonMain.dependencies {
            // Add api dependencies as needed (e.g., core:centerpost for interactors)
        }
    }
}
```

For pure contract modules with no DI needs, use `mockdonalds.kmp.library` instead.

**impl/build.gradle.kts** — concrete implementations:
```kotlin
plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.{name}.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:{name}:api"))
        }
    }
}
```

**test/build.gradle.kts** (if `hasTest` is true) — fakes for consumer tests:
```kotlin
plugins {
    id("mockdonalds.kmp.domain")
}

kotlin {
    android {
        namespace = "com.mockdonalds.app.core.{name}.test"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:{name}:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
```

### 2. Add Includes to settings.gradle.kts

Core modules are manually listed (not auto-discovered). Add to the "Core modules" section:

```kotlin
include(":core:{name}:api")
include(":core:{name}:impl")
include(":core:{name}:test")  // if hasTest
```

### 3. Create Source Files

**api/** — `src/commonMain/kotlin/com/mockdonalds/app/core/{name}/`

Place public interfaces, abstract interactors, and data types here. Package: `com.mockdonalds.app.core.{name}`.

- Interfaces define the contract — consumers depend only on these
- Abstract interactors extend `CenterPostInteractor` or `CenterPostSubjectInteractor`
- Data classes use `@Serializable` if they cross module boundaries

**impl/** — `src/commonMain/kotlin/com/mockdonalds/app/core/{name}/impl/`

Place concrete implementations here. Package: `com.mockdonalds.app.core.{name}.impl`.

Key rules:
- All implementations: `@ContributesBinding(AppScope::class)` (Metro implicitly provides `@Inject` — do NOT add explicit `@Inject`)
- Singletons: add `@SingleIn(AppScope::class)`
- Internal types that consumers should never see: mark `internal`
- Interactor impls follow the pattern:
  ```kotlin
  @ContributesBinding(AppScope::class)
  class {Name}Impl(
      private val dependency: SomeDependency,
  ) : {Name}() {
      override fun createObservable(params: P): Flow<T> {
          return dependency.observe()
      }
  }
  ```

**test/** — `src/commonMain/kotlin/com/mockdonalds/app/core/{name}/test/`

Place fakes here (in `commonMain`, NOT `commonTest`). Package: `com.mockdonalds.app.core.{name}.test`.

- Fakes are `MutableStateFlow`-backed with control methods (`setXxx()`, `reset()`)
- Annotate with `@ContributesBinding(AppScope::class)` for test graph auto-wiring
- Every public interface and abstract interactor in api needs a corresponding fake

### 4. Create Tests

Place unit tests in `impl/src/commonTest/kotlin/com/mockdonalds/app/core/{name}/impl/`.

- Use Kotest `BehaviorSpec` with `Given`/`When`/`Then` structure
- Use inline anonymous-object fakes for internal interfaces
- Test all public behavior of each impl class

### 5. Create AGENTS.md

Create `core/{name}/AGENTS.md` following this template:

```markdown
# core:{name}

## Purpose

One-line description of what this core module provides.

## Architecture

\```
core/{name}/api   -> Public interfaces and types (feature-visible)
core/{name}/impl  -> Concrete implementations (DI-only, never imported directly)
core/{name}/test  -> Fakes for consumer tests
\```

## Public API

| Type | Module | Description |
|------|--------|-------------|
| `{Interface}` | api | Description |
| `{Interactor}` | api | Abstract CenterPost interactor for ... |

## Usage

Features inject interfaces from api only:

\```kotlin
class MyPresenter(
    private val myInteractor: {Interactor},
) : ...
\```

## Rules

- Core modules never import from features
- Features MUST depend on `core:{name}:api` only, never `core:{name}:impl`
- `impl` is wired exclusively through Metro `@ContributesBinding` in `AppScope`
- Test code should use fakes from `core:{name}:test`
```

### 6. Wire into composeApp

Add the impl dependency to `composeApp/build.gradle.kts` in the core dependencies section:

```kotlin
implementation(project(":core:{name}:impl"))
```

This ensures the `@ContributesBinding` classes are on the classpath when `ProdAppGraph` is compiled. The api module is pulled transitively.

### 7. Verify Auto-Discovery

Confirm the modules are recognized:
```bash
./gradlew projects | grep {name}
```

Should show all submodules (api, impl, test if applicable).

## Key Rules

- Package convention: `com.mockdonalds.app.core.{name}` (api), `.impl` (impl), `.test` (test)
- No `@Inject` on impl classes — `@ContributesBinding` handles it implicitly
- Fakes live in `test/src/commonMain/` — they are published dependencies, not test-only
- AGENTS.md is required (Konsist-enforced via `AgentDocumentationTest`)
- Core modules NEVER import from feature modules
- `AppGraph` in `core:metro` does NOT need updating — consumers get dependencies via constructor injection

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the full `verify` skill (not `verify-smart`) since new modules touch build structure and require full validation.

The `verify` skill will run: lint, unit tests, architecture tests (Konsist + Harmonize), and builds for both platforms.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
