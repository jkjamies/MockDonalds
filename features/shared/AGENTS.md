# Shared Features Group

## Business Context
`features/shared/` groups feature modules whose actual screens, presenters, or data layers are consumed by **both** the mobile app and the kiosk app. The bar for living here is real cross-host consumption — not "could be shared one day." If only mobile consumes it, it goes in `features/mobile/`; only kiosk → `features/kiosk/`.

## Boundary
- Shared features must NOT import `features.mobile.*` or `features.kiosk.*` — that would pin a "shared" thing to one app's specifics. Konsist (`KioskBoundaryTest`) enforces.
- Shared features may freely import `core.*` and other `features.shared.*`.

## Children

| Module | Notes |
|---|---|
| `features/shared/menu` | Single source of menu data (categories, items, featured items, cart summary, `GetOrderContent`, `OrderRepository`). Consumed by `mobile/order`'s `OrderPresenter` and `kiosk/order`'s `KioskOrderPresenter` — each app has its own screen and UI on top of the same domain. |

## Submodule Shape
Shared features may carry any subset of the 6 submodules — the `walkTopDown` auto-discovery in `settings.gradle.kts`, `composeApp/build.gradle.kts`, and `kioskComposeApp/build.gradle.kts` only includes what exists. Two common shapes:

- **Domain-only** (e.g., `shared/menu`): `api/domain` + `impl/data` + `impl/domain` + `test`. No `api/navigation` or `impl/presentation` — the screens live in each consuming app's feature.
- **Full feature**: all 6 submodules. Use this when the screen itself is rendered identically in both apps.

## Hosting
Both `:composeApp` and `:kioskComposeApp` walk `features/shared/` and consume every submodule with a `build.gradle.kts`.

## Per-Feature Context
Per-feature business context, key types, and cross-feature dependencies live in each child's `AGENTS.md` (e.g., `features/shared/menu/AGENTS.md`).
