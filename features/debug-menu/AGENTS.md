# Debug Menu Feature

## Business Context
The Debug Menu is an engineer-facing surface that exposes inspection and override tools (feature flags, build config, …) without affecting production users. It is contributed into the More tab via `MoreTabExtension` with `isDebugOnly = true`, so `MorePresenter` hides it whenever `AppBuildConfig.isDebug` is false — the entry and all of its sub-screens are unreachable in release builds without any compile-time stripping.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| DebugMenuScreen | api/navigation | `data object`, root destination for the debug hub |
| FeatureFlagsDebugScreen | api/navigation | `data object`, feature-flag inspector sub-screen |
| BuildConfigDebugScreen | api/navigation | `data object`, build-config inspector sub-screen |
| DebugMenuTestTags | api/ui | Test tags for `DebugMenuUi` (root, entry list/item, back button) |
| FeatureFlagsDebugTestTags | api/ui | Test tags for `FeatureFlagsDebugUi` |
| BuildConfigDebugTestTags | api/ui | Test tags for `BuildConfigDebugUi` |
| DebugMenuEntry | impl/presentation | `id`, `title`, `subtitle`, `target: Screen` — one row in the hub list |
| DebugMenuPresenter | impl/presentation | Builds the entry list, routes `EntryClicked` to the target screen and `BackClicked` to `navigator.pop()` |
| BuildConfigDebugPresenter | impl/presentation | Injects `AppBuildConfig` and publishes `asFields()` (from `core:build-config:api`) as read-only rows grouped by `BuildConfigField.Group` |
| FeatureFlagsDebugPresenter | impl/presentation | Injects `Set<FeatureFlagDefinition>` (Metro multibinding) + `RemoteConfigProvider`; produces `FeatureFlagRow` entries sorted by owner then key, resolved via `rememberFlag` for per-flag recomposition |
| FeatureFlagRow | impl/presentation | `key`, `description`, `owner`, `lifecycle`, `enabled` — one row in the flags screen |
| DebugMenuUi / BuildConfigDebugUi / FeatureFlagsDebugUi | impl/presentation (androidMain) | Compose UIs, each annotated `@CircuitInject` |
| DebugMenuTabExtension | impl/presentation | `@ContributesIntoSet(AppScope::class)` — adds the 🐞 Debug Menu row to the More tab (`isDebugOnly = true`) |

## Cross-Feature Dependencies
- Contributes into: `features:more` via `MoreTabExtension` (runtime `Set<MoreTabExtension>` multibinding)
- Navigates to: its own sub-screens (`FeatureFlagsDebugScreen`, `BuildConfigDebugScreen`) — no outbound cross-feature navigation
- Imported by: composeApp (wired at app level, scoped to debug builds via the extension's `isDebugOnly` flag)
- Core deps: core:centerpost, core:theme, core:remote-config:api (flag registry + rememberFlag), core:build-config:api (AppBuildConfig + asFields)
- Sibling api deps: `:features:more:api:navigation` (to implement `MoreTabExtension`)

## Feature-Specific Patterns
- **Debug-only visibility is runtime, not compile-time.** `DebugMenuTabExtension.isDebugOnly = true` causes `MorePresenter` to filter the entry out when `AppBuildConfig.isDebug` is false. The debug-menu module still compiles and ships in release builds — it simply has no reachable entry point. This keeps the gate simple (no per-variant source sets, no `buildTypes {}` carve-outs) and lets the same APK be flipped into "debug mode" by swapping `AppBuildConfig`.
- **Hub + leaf structure.** `DebugMenuPresenter` owns the entry list (hard-coded today) and dispatches to leaf presenters via `navigator.goTo(entry.target)`. Adding a new debug tool = (1) add a `Screen` in api/navigation, (2) add a presenter + Ui + TestTags for it, (3) append an entry to the list in `DebugMenuPresenter`.
- **iOS parity.** SwiftUI `DebugMenuView`, `FeatureFlagsDebugView`, and `BuildConfigDebugView` under `iosApp/iosApp/Views/DebugMenu/` wrap the KMP presenters and are gated by `#if DEBUG` at their call sites.

## Testing
- Presenter: impl/presentation/src/commonTest/ (`DebugMenuPresenterTest`, `FeatureFlagsDebugPresenterTest`, `BuildConfigDebugPresenterTest`)
- UI (Android): impl/presentation/src/androidDeviceTest/ (`DebugMenuUiTest` / `UiRobot` / `StateRobot`, same trio for each sub-screen)
- UI (iOS): `iosAppTests/UIComponent/DebugMenuViewTest.swift` + sub-screen view tests (ViewInspector)
- E2E: `DebugMenuTestTags` referenced from a journey in `testing/e2e-tests/` so the arch suite keeps the tags live
- No data or domain layers today — the feature is presentation-only; when the flag-override or build-config listing gains state, the usual impl/domain + impl/data + test Fakes should follow the More-feature template.
