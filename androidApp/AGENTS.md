# Android App (Platform Shell)

## Purpose

Minimal Android platform shell. Contains the Activity, Application class, manifest, splash screen, and resources. No business logic belongs here -- all shared code lives in `composeApp` and feature modules.

## Key Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Single Activity. Installs splash screen, enables edge-to-edge, delegates to `MockDonaldsApp` composable from `composeApp`. Passes `deepLinkIntent` and `WindowSizeClass` to the shared composable. Handles `onNewIntent` for deep link re-entry (`singleTop` launch mode). |
| `MockDonaldsApplication.kt` | Empty `Application` subclass -- placeholder for future app-level init (crash reporting, logging, etc.). |
| `AndroidManifest.xml` | Declares `MainActivity` as launcher with deep link intent filter (`mockdonalds://app`). Enables predictive back (`enableOnBackInvokedCallback`). |
| `build.gradle.kts` | Android application plugin, depends on `:composeApp`. R8/ProGuard enabled for release builds. |
| `proguard-rules.pro` | ProGuard keep rules for release minification. |

## Resources

| Resource | Purpose |
|----------|---------|
| `themes.xml` | Base `Theme.MockDonalds` (Material3, no action bar) and `Theme.MockDonalds.Splash` (splash screen theme with animated icon) |
| `splash_logo_animated.xml` / `splash_logo.xml` | Animated vector drawable for splash screen branding |
| `ic_launcher_foreground.xml` | Adaptive icon foreground |
| `colors.xml` | Splash screen background color (`#FF2B2B2B`) |

## Deep Linking

Deep links arrive as intents with scheme `mockdonalds` and host `app`. The flow:

1. `MainActivity` captures the intent (both cold start via `onCreate` and warm via `onNewIntent`)
2. Passes it as `deepLinkIntent` to `MockDonaldsApp` in `composeApp`
3. `composeApp` parses the URI via `DeepLinkParser` and routes through `InterceptingNavigator`

## Key Conventions

- This module is a thin shell -- never add Compose UI, navigation, or domain logic here
- All UI composition happens in `composeApp` via the `MockDonaldsApp` composable
- Window size class is calculated here and passed down for adaptive layout decisions
