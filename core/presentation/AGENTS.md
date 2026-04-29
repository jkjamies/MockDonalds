# core:presentation

## Purpose

Cross-cutting Compose-specific helpers and extensions over otherwise Compose-free core modules. Lets non-presentation core modules (`core:centerpost`, `core:remote-config:api`, future `core:analytics:api`, etc.) stay free of Compose runtime while still offering Composable extension points to presentation layers.

This module is the only Compose-aware seam over otherwise plain-Kotlin core APIs. Convention plugin `mockdonalds.kmp.presentation` auto-wires it into every feature `impl/presentation` module, so feature presenters get `rememberFlag`, `rememberConfig`, `rememberCenterPost`, and `CenterPostSubjectInteractor.collectAsState` automatically — no per-feature dep declaration required.

## Architecture

```
core/presentation/
  build.gradle.kts                     kmp.library + Compose plugin (no Metro, no Circuit)
  src/commonMain/kotlin/com/mockdonalds/app/core/presentation/
    centerpost/
      RememberCenterPost.kt            @Composable rememberCenterPost(dispatchers): CenterPost
      CollectAsState.kt                @Composable CenterPostSubjectInteractor<Unit, T>.collectAsState(initial)
    remoteconfig/
      RememberFlag.kt                  @Composable RemoteConfigProvider.rememberFlag(flag): State<Boolean>
      RememberConfig.kt                @Composable RemoteConfigProvider.rememberConfig(config): State<T>
```

The package layout mirrors the source core module (`centerpost/`, `remoteconfig/`) so the Compose layer stays organized as new core modules contribute Composable extensions.

## Public API

| Type | Subpackage | Description |
|------|-----------|-------------|
| `rememberCenterPost(dispatchers)` | `centerpost` | Compose-scoped factory for `CenterPost`; survives recomposition, cancels with the host |
| `CenterPostSubjectInteractor<Unit, T>.collectAsState(initial)` | `centerpost` | Compose extension that triggers the interactor with `Unit` and collects its `Flow<T>` as `State<T?>` |
| `RemoteConfigProvider.rememberFlag(flag)` | `remoteconfig` | Returns `State<Boolean>` seeded with `flag.defaultValue`, recomposes on remote change |
| `RemoteConfigProvider.rememberConfig(config)` | `remoteconfig` | Returns `State<T>` seeded with `config.default` for any `RemoteConfig<T>` subtype |

## Wiring

`core:presentation` is auto-wired by the convention plugin — no per-module dep needed:

```kotlin
// build-logic/convention/.../mockdonalds.kmp.presentation.gradle.kts
sourceSets {
    commonMain {
        dependencies {
            implementation(project(":core:presentation"))
            // ...
        }
    }
}
```

So any feature `impl/presentation` module that applies `mockdonalds.kmp.presentation` automatically sees `rememberFlag`, `rememberConfig`, `rememberCenterPost`, and `collectAsState`. Just import them.

## Rules

- **Compose imports are ONLY allowed here, in `features/*/impl/presentation`, `core:theme`, `core:circuit`, `composeApp`, `androidApp`, and `testing/navint-tests`** — Konsist-enforced via `ComposeIsolationTest`. Any new Composable extension on a core type belongs in this module, not in the source core module.
- This module depends only on `core:centerpost` and `core:remote-config:api` today. Any future Composable extension over a new core type adds an `api(...)` dep on that core's `:api` module — never on its `:impl`.
- No Metro DI annotations here — this is extension functions only. No Circuit either; presenter wiring stays in feature presentation modules.
- Package layout: one subpackage per source core module (`com.mockdonalds.app.core.presentation.{coreModuleName}`). Keeps related extensions colocated and lets the source core module be discovered from the import path.
- The seam between Compose-aware and Compose-free is **the source core module's api/impl boundary** — `core:centerpost` and `core:remote-config:api` stay Compose-free; their Composable extensions live here. `ComposeIsolationTest` enforces the rule.
