# iosApp/iosApp/Presentation

## Purpose

iOS sibling to Kotlin `core:presentation`. Houses cross-cutting SwiftUI primitives that don't belong inside a single feature — embedded WebView, future shared image loaders, shared loading/error states.

The Kotlin side renders Android-native primitives in `core:presentation/src/androidMain/`; this folder owns the iOS-native counterparts. APIs are conceptually symmetric per primitive but adopt each platform's idioms (Compose vs SwiftUI, callbacks vs bindings).

## Conventions

- One subfolder per primitive concept (`WebView/`, future `Loading/`, `ImageLoader/`, etc.).
- Primitives are pure SwiftUI views — no business logic, no domain types, no KMP imports beyond shared models passed in by the consumer.
- Primitives have no per-feature knowledge; consumers wire them into their own SwiftUI scaffolds.
- New files are picked up automatically by `xcodegen` because `iosApp/project.yml` declares `path: iosApp` for sources. After adding files, run `xcodegen` (or `xcodegen generate`) to regenerate `iosApp.xcodeproj`.
- Naming mirrors the Kotlin side where it makes sense (`WebView` matches `WebViewContent` conceptually but adopts SwiftUI idioms in API shape).

## Current contents

| Folder | Purpose |
|--------|---------|
| `WebView/` | `UIViewRepresentable` over `WKWebView` for rendering web content inline (T&C, PP, nutrition, FAQ articles). Mirrors `core:presentation`'s Android `WebViewContent`. |

## Public API by primitive

### `WebView/WebView.swift`

| Type | Description |
|------|-------------|
| `WebView(url, allowJs, onExternalLink, onError, canGoBack, errorView)` | SwiftUI view wrapping `WKWebView` with `.nonPersistent()` data store. JS off by default. Off-host links open via `UIApplication.shared.open(_:)` unless `onExternalLink` returns `true`. |
| `WebViewError(url, code, description)` | Surfaced via `onError` callback for terminal navigation failures. |

Hosts wire back navigation via the `@Binding var canGoBack` and a toolbar button — `NavigationStack` edge-swipe always pops the host stack (platform-native, not overridable).
