# Kiosk Features Group

## Business Context
`features/kiosk/` groups the three feature modules that make up the kiosk app surface (`features/kiosk/{attract, identify, order}`). The grouping is structural rather than architectural — each child is a normal 6-module feature; settings.gradle.kts walks this subdirectory separately so kiosk features can be hosted only by `kioskComposeApp` and excluded from the consumer `composeApp` graph at compile time.

## Boundary
- Consumer host (`composeApp`) must NOT import any `features/kiosk/*` symbol — Konsist (`KioskBoundaryTest`) enforces.
- Kiosk features must NOT import any consumer-only feature (home/more/rewards/profile/recents/scan/login/debug-menu/nutrition) — Konsist enforces.
- Reuse from kiosk → consumer goes through `core/*` and `features/order/api/*` only (kiosk reuses the consumer menu domain layer).

## Children

| Module | AGENTS.md | Notes |
|---|---|---|
| `features/kiosk/attract` | [AGENTS.md](attract/AGENTS.md) | Navigation root for the kiosk; rotating ad carousel + Touch-to-Order overlay. |
| `features/kiosk/identify` | [AGENTS.md](identify/AGENTS.md) | Phone keypad + simulated QR scanner + skip CTA. Funnels through `core:auth.AuthManager`. |
| `features/kiosk/order` | [AGENTS.md](order/AGENTS.md) | Vertical NavigationRail + 3-col item grid + cart bar. Reuses `features/order/api/domain.GetOrderContent`. |

## Hosting
Hosted exclusively by `kioskComposeApp` (which is consumed by `kioskApp`). The peer host `composeApp` (consumed by `androidApp`) hosts the consumer features instead.

## Test Infra
Kiosk has its own peer test suites mirroring the consumer's:
- `:testing:kiosk:navint-tests` — real presenters + fake data layer, Compose-driven navigation tests.
- `:testing:kiosk:e2e-tests` — UI Automator journey tests against the installed `:kioskApp` debug APK.
- `:testing:kiosk:benchmarks` — macrobenchmarks against the kiosk app's `benchmark` build variant.
- Konsist boundary rules live in `:testing:architecture-check`'s `kiosk/` subpackage (`KioskBoundaryTest`).
