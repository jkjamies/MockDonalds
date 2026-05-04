# kioskComposeApp

## Purpose
KMP library hosting the kiosk Compose root, kiosk Metro graph, kiosk SQLDelight `AppDatabase`, and kiosk-specific shell concerns (idle timer). Consumed by `:kioskApp` (the AGP application) the way `:composeApp` is consumed by `:androidApp`. Android target only initially — adding iOS later is purely additive (declare iOS targets, add bridge files).

## Cross-app Boundary
- Auto-discovers `features/kiosk/{name}/*` and `features/shared/{name}/*` modules. The `features/shared/` walk is shared with `composeApp` — anything genuinely multi-host lives there.
- Forbidden: any `features/mobile/*` import — Konsist (`KioskBoundaryTest`) enforces. Cross-host reuse goes through `features/shared/*` only.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| `MockDonaldsKioskApp` | `androidMain` | Compose root. Creates `ProdKioskAppGraph` from `Application`, initializes logger, wraps `MockDonaldsTheme`, builds `rememberSaveableBackStack(root = AttractScreen)` + bare `rememberCircuitNavigator` (no `InterceptingNavigator`, no `AuthInterceptor`, no `DeepLinkParser`, no bottom bar — kiosk doesn't need any of those). |
| `ProdKioskAppGraph` | `androidMain` | `@DependencyGraph(AppScope::class) interface ProdKioskAppGraph : AppGraph { val kioskIdleTimer: KioskIdleTimer }`. Factory takes `Application`. |
| `KioskIdleTimer` | `commonMain` | `@Inject @SingleIn(AppScope::class)`. Phase-1 stub with `poke()` no-op; Phase 5 fills with remote-config-driven per-screen timeouts and emits expirations that the host catches → `resetRoot(AttractScreen)`. |
| SQLDelight `AppDatabase` | `commonMain/sqldelight/` | Aggregator block in `build.gradle.kts` declares `databases { create("AppDatabase") { packageName.set("com.mockdonalds.kiosk.persistence") } }`. Kiosk-only schemas live under `features/kiosk/{name}/impl/data/src/commonMain/sqldelight/`. Separate from the consumer's `AppDatabase` — kiosk runs in its own Android sandbox. |

## Why a separate host (not reuse :composeApp)
Two reasons:
1. `composeApp`'s auto-discovery walks every `features/*` directory and depends on it at compile time. Reusing `composeApp` would mix consumer + kiosk feature classes in both APKs at compile time. R8 strips at release, but the compile-time graph stays mixed and Konsist boundary rules become weaker.
2. Kiosk's host surface is fundamentally smaller — no auth interceptor, no deep-link parser, no `InterceptingNavigator`, no bottom-nav. Putting both in `composeApp` makes that file a polymorphic switch, not a focused entry point.

A peer host keeps dependency graphs disjoint and the surface honest.

## Spec
Full spec: [`specs/kiosk-app.md`](../specs/kiosk-app.md).
