# Kiosk Attract Feature

## Business Context
The kiosk attract screen is the navigation root of the kiosk app — what guests see when no one is interacting with the kiosk. It rotates promotional ads (Image 1, Image 2 source references) and dispatches a `TouchToOrder` event on any tap, navigating to `IdentifyScreen`. After idle expiry on any later screen, the kiosk navigator resets the back stack to `AttractScreen`.

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| AttractScreen | api/navigation | `data object`, plain `Screen`. Navigation root for `kioskComposeApp`. |
| AttractTestTags | api/navigation (`api/ui` subpackage) | `SCREEN`, `PAGER`, `TOUCH_TO_ORDER_OVERLAY`, `AD_IMAGE` constants. |
| AttractContent | api/domain | `ads: List<Ad>`, `rotationSeconds: Int`. |
| Ad | api/domain | `id`, `imageUrl`, `headline`, `subheadline?` — no `backgroundColorHex` (dropped during grill). |
| GetAttractContent | api/domain → impl/domain | `CenterPostSubjectInteractor<Unit, AttractContent>`. |
| GetAttractContentImpl | impl/domain | Forwards `AttractRepository.getAttractContent()` unchanged. |
| AttractRepository | impl/domain → impl/data | `getAttractContent(): Flow<AttractContent>`. |
| AttractRepositoryImpl | impl/data | Phase-1 static fallback (3 placeholder ads, sentinel URLs). Production source per spec is a marketing platform (Braze etc.) feeding remote-config — interface is the swap seam. |
| AttractPresenter | impl/presentation | `@CircuitInject(AttractScreen)`. Tracks `currentIndex` locally; phase-1 navigator is wired in phase 2 alongside the IdentifyScreen target. |
| AttractUiState | impl/presentation | `data class(ads, currentIndex, rotationSeconds, eventSink)`. |
| AttractEvent | impl/presentation | sealed: `TouchToOrder`, `IndexChanged(index)`. |
| AttractUi | impl/presentation/androidMain | Phase-1 placeholder: full-bleed `Box` with current-ad headline + "Touch to Order" overlay. Phase 2 swaps in `HorizontalPager` + `AsyncImage` + auto-rotation. |
| FakeGetAttractContent | test | `@ContributesBinding(AppScope::class)`. `DEFAULT.ads = [TEST HEADLINE]` — single sentinel ad. |

## Cross-Feature Dependencies
- Imported by: `kioskComposeApp` only (auto-discovered via `features/kiosk` walk in `kioskComposeApp/build.gradle.kts`).
- Navigates to: `IdentifyScreen` (phase 2 wiring; phase 1 leaves the navigator unwired).
- Core deps: `core:centerpost`, `core:circuit`, `core:theme`, `core:network:api`, `core:build-config:api`.
- Konsist enforces: this feature must not import any consumer-only feature (home, more, rewards, profile, recents, scan, login, debug-menu, nutrition).

## Feature-Specific Patterns
- **Sentinel image URLs in v1.** All ad image URLs use `https://example.test/` (RFC2606 reserved domain) — placeholder dev hygiene; real ads come from a marketing platform later.
- **No deep link.** Kiosks launch from the launcher; attract is the post-launch root, not a URI target.
- **Idle timer disabled here.** `KioskIdleTimer` runs on Identify and KioskOrder, never on Attract — Attract IS the idle state.
- **Phase split.** Phase 1 lands the structural scaffold (one real `@CircuitInject` presenter to satisfy `core:circuit`'s presenter-factories multibinding contract). Phase 2 polishes the UI into a full-bleed ad carousel with auto-rotation and brand overlay.
