# core:presentation

## Purpose

Cross-cutting Compose-specific code: extensions over otherwise Compose-free core modules **and standalone UI primitives** that don't belong inside a single feature. Lets non-presentation core modules (`core:centerpost`, `core:remote-config:api`, future `core:analytics:api`, etc.) stay free of Compose runtime while still offering Composable extension points to presentation layers, and provides reusable UI primitives (e.g., embedded WebView) shared across multiple features.

This module is the only Compose-aware seam over otherwise plain-Kotlin core APIs. Convention plugin `sampleplatter.kmp.presentation` auto-wires it into every feature `impl/presentation` module, so feature presenters get `rememberFlag`, `rememberConfig`, `rememberCenterPost`, `CenterPostSubjectInteractor.collectAsState`, and `WebViewContent` automatically — no per-feature dep declaration required.

## Architecture

```
core/presentation/
  build.gradle.kts                     kmp.library + Compose plugin (no Metro, no Circuit)
  src/commonMain/kotlin/com/jkjamies/sampleplatter/core/presentation/
    centerpost/
      RememberCenterPost.kt            @Composable rememberCenterPost(dispatchers): CenterPost
      CollectAsState.kt                @Composable CenterPostSubjectInteractor<Unit, T>.collectAsState(initial)
    remoteconfig/
      RememberFlag.kt                  @Composable RemoteConfigProvider.rememberFlag(flag): State<Boolean>
      RememberConfig.kt                @Composable RemoteConfigProvider.rememberConfig(config): State<T>
  src/androidMain/kotlin/com/jkjamies/sampleplatter/core/presentation/
    webview/
      WebViewContent.kt                @Composable wrapping AndroidView { WebView }
      WebViewState.kt                  @Stable holder + rememberWebViewState() — exposes canGoBack/goBack
      WebViewError.kt                  data class (url, code, description)
      internal/
        WebViewLoadingIndicator.kt     LinearProgressIndicator overlay
        WebViewErrorView.kt            built-in error layout (icon + message + retry button)
        WebViewClientFactory.kt        WebViewClient subclass — off-host handoff via Custom Tabs
```

The package layout mirrors the source core module (`centerpost/`, `remoteconfig/`) for extensions, while standalone primitives (`webview/`) live in `androidMain` because they render Android-native UI. The iOS sibling for cross-cutting SwiftUI primitives lives at `iosApp/iosApp/Presentation/` (see `iosApp/iosApp/Presentation/AGENTS.md`).

## Public API

| Type | Source set | Subpackage | Description |
|------|-----------|-----------|-------------|
| `rememberCenterPost(dispatchers)` | commonMain | `centerpost` | Compose-scoped factory for `CenterPost`; survives recomposition, cancels with the host |
| `CenterPostSubjectInteractor<Unit, T>.collectAsState(initial)` | commonMain | `centerpost` | Compose extension that triggers the interactor with `Unit` and collects its `Flow<T>` as `State<T?>` |
| `RemoteConfigProvider.rememberFlag(flag)` | commonMain | `remoteconfig` | Returns `State<Boolean>` seeded with `flag.defaultValue`, recomposes on remote change |
| `RemoteConfigProvider.rememberConfig(config)` | commonMain | `remoteconfig` | Returns `State<T>` seeded with `config.default` for any `RemoteConfig<T>` subtype |
| `WebViewContent(url, modifier, allowJs, onExternalLink, onError, state, errorContent)` | **androidMain** | `webview` | Embedded `AndroidView { WebView }`. JS off by default. Off-host links open in Custom Tabs unless `onExternalLink` returns true. Built-in loading + error UX with override slot. Ephemeral cookies (cleared on dispose). |
| `rememberWebViewState()` | **androidMain** | `webview` | Creates a `WebViewState` holder for back-nav coordination |
| `WebViewState.canGoBack` / `WebViewState.goBack()` | **androidMain** | `webview` | Reflects WebView internal history; host wires `BackHandler(enabled = state.canGoBack) { state.goBack() }` |
| `WebViewError(url, code, description)` | **androidMain** | `webview` | Surfaced via `onError` callback for terminal navigation failures |

## Wiring

`core:presentation` is auto-wired by the convention plugin — no per-module dep needed:

```kotlin
// build-logic/convention/.../sampleplatter.kmp.presentation.gradle.kts
sourceSets {
    commonMain {
        dependencies {
            implementation(project(":core:presentation"))
            // ...
        }
    }
}
```

So any feature `impl/presentation` module that applies `sampleplatter.kmp.presentation` automatically sees `rememberFlag`, `rememberConfig`, `rememberCenterPost`, `collectAsState`, and (in `androidMain`) `WebViewContent`. Just import them. The Compose UI deps `WebViewContent` requires (`compose.foundation`, `compose.ui`, `compose.material3`) are already pulled into feature `androidMain` by the same convention plugin, so no extra dep wiring is required at the feature level.

`androidMain` deps owned by `core:presentation` itself (Compose UI + `androidx.browser:browser` for Custom Tabs) are internal to this module — feature consumers do not need to declare them.

## Rules

- **Compose imports are ONLY allowed here, in `features/*/impl/presentation`, `core:theme`, `core:circuit`, `composeApp`, `androidApp`, and `testing/navint-tests`** — Konsist-enforced via `ComposeIsolationTest`. Any new Composable extension on a core type or standalone UI primitive belongs in this module, not in the source core module.
- This module depends only on `core:centerpost` and `core:remote-config:api` today. Any future Composable extension over a new core type adds an `api(...)` dep on that core's `:api` module — never on its `:impl`.
- No Metro DI annotations here — this module is **Compose extensions and UI primitives only**. No Circuit either; presenter wiring stays in feature presentation modules.
- Package layout: one subpackage per source core module for extensions (`com.jkjamies.sampleplatter.core.presentation.{coreModuleName}`); one subpackage per primitive concept for standalone UI (`webview/`, future `loading/`, etc.). Internal helpers go under an `internal/` subpackage.
- The seam between Compose-aware and Compose-free is **the source core module's api/impl boundary** — `core:centerpost` and `core:remote-config:api` stay Compose-free; their Composable extensions live here. `ComposeIsolationTest` enforces the rule.
- Standalone UI primitives that render native UI live in `androidMain`. Their iOS counterparts live at `iosApp/iosApp/Presentation/{Concept}/` as SwiftUI views (see iOS Presentation AGENTS.md).
