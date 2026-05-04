# kioskApp

## Purpose
Android-only AGP application target for in-restaurant self-order kiosks (vertical 32" touchscreens, portrait-locked, always-on, immersive, lock-task-friendly). Thin shell — application class + activity + manifest + launcher icons + theme + 30-variant flavor matrix. All UI, DI, and feature wiring live in `:kioskComposeApp`.

## Cross-app Boundary
- Hosted features: `features/kiosk/{attract, identify, order}` only — Konsist (`KioskBoundaryTest`) enforces.
- Reuses: `features/order/api/*` (menu domain) + everything in `core/*`.
- Forbidden: any `features/{home, more, rewards, profile, recents, scan, login, debug-menu, nutrition}` import; any `TabScreen` reference.

## Files

| File | Notes |
|------|-------|
| `KioskApplication.kt` | `android.app.Application` — Metro graph is created in the activity, not here. |
| `KioskMainActivity.kt` | `ComponentActivity`. `onCreate` calls `installSplashScreen()` (must run before `super.onCreate`) so the AndroidX splash hands off cleanly into Compose, then configures portrait lock, immersive system bars (`hide(systemBars())` + `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`), `FLAG_KEEP_SCREEN_ON`, then calls `MockDonaldsKioskApp(application)`. |
| `AndroidManifest.xml` | Launcher intent-filter, `screenOrientation="portrait"`, `resizeableActivity="false"`, activity `theme=@style/Theme.MockDonalds.Kiosk.Splash`. **No camera permission v1** — QR scanner is a simulated placeholder; permission added when CameraX integration ships. **No deep-link intent-filter** — kiosks launch from the launcher only. |
| `build.gradle.kts` | Mirrors `androidApp`'s plugin block + 30-variant flavor matrix (5 markets × 3 envs × debug/release/benchmark). `applicationId = "com.mockdonalds.kiosk"` with per-market suffix. Pulls `androidx.core.splashscreen` in for the SplashScreen compat library. |
| `proguard-rules.pro` | Empty (release uses default rules). |
| `res/values/themes.xml` | `Theme.MockDonalds.Kiosk` (post-splash app theme, transparent system bars) + `Theme.MockDonalds.Kiosk.Splash` (extends `Theme.SplashScreen`, deep-obsidian background, animated M-arches via `splash_logo_animated`, `postSplashScreenTheme = Theme.MockDonalds.Kiosk`). |
| `res/drawable/` | `splash_logo.xml` (M-arches static) + `splash_logo_animated.xml` (trim-path animation, 1000ms) + `ic_launcher_foreground.xml`. Same artwork as the consumer splash — duplicated rather than shared because each app has its own `res/` tree and `androidx.core.splashscreen` reads from the consuming module. |

## Variants
30 variants: `{us,ca,de,au,core}` × `{int,mte,prod}` × `{debug,release,benchmark}`. `BuildVariantResolver.appType(project)` returns `"Kiosk"` whenever a `:kioskApp:` or `:kioskComposeApp:` task name is in the active task list, so `BuildKonfig.APP_TYPE` resolves correctly.

## Hardware Deployment Notes
Production kiosk deployment uses a device-owner DPC to enable lock-task mode (`DevicePolicyManager.setLockTaskPackages`) and disable the keyguard — that's deployment infra, not configured by this module. The app is friendly to lock-task mode out of the box (single-task launch, no system-bar dependency).

## Spec
Full spec: [`specs/kiosk-app.md`](../specs/kiosk-app.md).
