# Mobile Features Group

## Business Context
`features/mobile/` groups feature modules that target the consumer mobile/iOS app surface. Each child here is a normal 6-submodule feature consumed by `:composeApp` (which is consumed by `:androidApp` and the iOS framework). settings.gradle.kts walks this subdirectory together with `features/shared/` for the consumer host's auto-discovery.

## Boundary
- Mobile features must NOT import any `features.kiosk.*` symbol — Konsist (`KioskBoundaryTest`) enforces.
- Mobile features may freely import `features.shared.*` (genuinely-shared things like the menu domain).
- Cross-feature imports inside `features/mobile/*` go through other features' `api/` modules only (Konsist `LayerDependencyTest`).

## Children

| Module | Notes |
|---|---|
| `features/mobile/debug-menu` | Engineer-facing build-config / feature-flag inspection (debug builds only). |
| `features/mobile/home` | Primary landing tab — greeting, hero promo, recents, explore grid. |
| `features/mobile/login` | Email + Apple + Google sign-in flow (`FlowScreen`). |
| `features/mobile/more` | Settings hub — profile summary + secondary navigation list. |
| `features/mobile/nutrition` | Embedded WebView pointing at the per-market nutrition calculator. |
| `features/mobile/order` | Consumer order screen (categories chip row + featured items + cart bar). Consumes `features/shared/menu` for the menu domain. |
| `features/mobile/profile` | Auth-gated user profile (`ProtectedScreen`). |
| `features/mobile/recents` | Recent activity / orders surface, reachable from More. |
| `features/mobile/rewards` | Loyalty progress, vault gallery, transaction history. |
| `features/mobile/scan` | Member QR display for in-store scanning. |

## Hosting
Hosted exclusively by `:composeApp` (consumed by `:androidApp` + the iOS framework). The peer host `:kioskComposeApp` (consumed by `:kioskApp`) hosts `features/kiosk/*`. Both hosts pull `features/shared/*`.

## Test Infra
- `:testing:mobile:navint-tests` — real consumer presenters + fake data layer.
- `:testing:mobile:e2e-tests` — UI Automator journeys against `:androidApp`.
- `:testing:mobile:benchmarks` — macrobenchmarks against the consumer's `benchmark` build variant.

## Per-Feature Context
Per-feature business context, key types, and cross-feature dependencies live in each child's `AGENTS.md` (e.g., `features/mobile/order/AGENTS.md`).
