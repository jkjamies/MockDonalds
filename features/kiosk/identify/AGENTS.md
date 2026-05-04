# Kiosk Identify Feature

## Business Context
The identify screen handles guest identification on the kiosk: scan a rewards QR code, enter a phone number, or skip and order anonymously. After successful identify (or skip), the navigator resets the back stack to the screen passed via `IdentifyScreen.next` (typically `KioskOrderScreen`); if `next` is null, the navigator pops back to the previous screen (Phase 3 default — Phase 4 wires `next = KioskOrderScreen` from the Attract presenter).

## Key Types

| Type | Location | Notes |
|------|----------|-------|
| IdentifyScreen | api/navigation | `data class(next: Screen? = null)`. Plain `Screen` (no auth gate, no flow). |
| IdentifyTestTags | api/navigation (`api/ui` subpackage) | Constants for keypad keys, phone display, scanner, skip, error banner, loading overlay. |
| IdentifyContent | api/domain | `skipEnabled`, `phoneEntryEnabled`, `qrScannerEnabled`, `countryDialCode`. |
| IdentifyResult | api/domain | sealed: `Identified(accountId)`, `GuestSession`, `Failed(reason)`. |
| GetIdentifyContent / IdentifyByPhoneNumber / IdentifyByQrCode / ContinueAsGuest | api/domain → impl/domain | Streaming + 3 one-shot use cases. |
| IdentifyRepository / IdentifyRepositoryImpl | impl/domain → impl/data | Phase-3 stub: `identifyByPhone` returns deterministic `Identified("kiosk-stub-${last4}")`; `identifyByQr` similar with payload prefix; `continueAsGuest` returns `GuestSession`. Real `/v1/kiosk/identify` backend wiring is a follow-up — surface stable. |
| IdentifyPresenter | impl/presentation | `@CircuitInject(IdentifyScreen)`. Holds `phoneInput` (rememberSaveable), `isSubmitting`, `errorMessage`. Submission goes through CenterPost. Validates `phoneInput.length >= 7` before submit. |
| IdentifyUiState / IdentifyEvent | impl/presentation | sealed event class (iOS interop); state has `phoneInput`, `isSubmitting`, `errorMessage`, gating flags, dial code, `eventSink`. |
| IdentifyUi | impl/presentation/androidMain | Header (yellow brand bar) + horizontal split body (QR scanner panel + phone keypad panel) + bottom skip CTA + loading overlay + auto-dismissing error banner. |
| FakeGetIdentifyContent / FakeIdentifyByPhoneNumber / FakeIdentifyByQrCode / FakeContinueAsGuest | test | All `@ContributesBinding(AppScope::class)`. Fake use cases expose mutable `nextResult` so tests can flip success/failure. |

## Cross-Feature Dependencies
- Imported by: `kioskComposeApp` (auto-discovered) and `features/kiosk/attract/impl/presentation` once Phase 3 wires `Attract → Identify` navigation.
- Navigates to: `screen.next` (passed in by caller; expected to be `KioskOrderScreen` once Phase 4 lands), else `navigator.pop()`.
- Core deps: `core:centerpost`, `core:circuit`, `core:theme`, `core:auth:api`, `core:network:api`, `core:build-config:api`.
- Konsist enforces: must not import any consumer-only feature.

## Feature-Specific Patterns
- **Per-market dial code from `core:build-config`** — `appBuildConfig.phoneCountryDialCode` ("+1" US/CA, "+49" DE, "+61" AU). No locale-derived runtime mapping.
- **Stubbed identify endpoint v1** — `/v1/kiosk/identify` doesn't exist yet; repository returns deterministic identified results so the UX layer is testable end-to-end. Swap is a single repository change with no presenter contract impact.
- **Simulated QR scanner v1** — no `<uses-permission CAMERA>` declared in `kioskApp/AndroidManifest.xml`; the QR panel renders a faux viewfinder with a "Simulate scan" button that dispatches a synthetic `QrCodeDetected("simulated-payload-${timestamp}")` so the post-identify flow is exercisable without real hardware. CameraX + ML Kit Barcode integration replaces the simulated path in a follow-up.
- **Auto-dismissing error banner** — `LaunchedEffect(state.errorMessage)` triggers `delay(4s) → DismissError`. Single source of truth for error UX; manual dismiss button also available.
- **Phone validation** — submit requires `phoneInput.length >= 7`; otherwise `errorMessage = "Enter at least 7 digits"`. Hard cap at 10 digits to keep keypad behavior predictable on US-format inputs.
- **`isSubmitting` gate** — while a use case is in-flight, all events are dropped (early return in event sink) and a translucent loading overlay covers the screen. Failed results clear the gate; success navigates away.

## Per-Market Configuration
- `phoneCountryDialCode` lives in `core:build-config` per-market. Defaults to `+1` (US/CA/CORE inheritance); DE overrides to `+49`, AU to `+61`.

## Phase Notes
- **Phase 3 (this initiative)**: full UI + stubbed backend + simulated QR. Navigator wiring from Attract → Identify is part of this phase.
- **Phase 4**: Attract presenter passes `IdentifyScreen(next = KioskOrderScreen)` once `KioskOrderScreen` exists.
- **Phase 5**: kiosk idle timer expiry resets to `AttractScreen` from this screen (60s default).
- **Future**: real `/v1/kiosk/identify` backend, CameraX + ML Kit QR scanner replacing the simulated panel.
