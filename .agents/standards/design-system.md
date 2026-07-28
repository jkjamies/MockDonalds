# Design System Reference

Unified design system across Android (Compose) and iOS (SwiftUI) with light/dark mode support driven by system preference.

## Theme Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                               Theme Stack                               │
│                                                                         │
│  ┌───────────────────────────────┐  ┌────────────────────────────────┐  │
│  │        Kotlin (Compose)       │  │        Swift (SwiftUI)         │  │
│  │                               │  │                                │  │
│  │  SamplePlatterTheme { }       │  │  .samplePlatterTheme()         │  │
│  │  ├── MaterialTheme            │  │  ├── SamplePlatterColorScheme  │  │
│  │  │   ├── colorScheme          │  │  │   ├── @Environment          │  │
│  │  │   └── typography           │  │  │   │   (\.colorScheme)       │  │
│  │  │       (Epilogue+Manrope)   │  │  │   └── System fonts          │  │
│  │  ├── ExtendedColors           │  │  ├── PlatterDimens enum        │  │
│  │  │   (CompositionLocal)       │  │  │   (matching Kotlin vals)    │  │
│  │  └── PlatterDimens object     │  │  └── AdaptiveLayout ext        │  │
│  │      (spacing, radii, sizes)  │  │      (orientation tokens)      │  │
│  └───────────────────────────────┘  └────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

| Layer | Kotlin (Compose) | Swift (SwiftUI) |
|-------|------------------|-----------------|
| **Color schemes** | `darkColorScheme()` / `lightColorScheme()` via `isSystemInDarkTheme()` | `SamplePlatterColorScheme` resolved via `@Environment(\.colorScheme)` |
| **Extended colors** | `SamplePlatterExtendedColors` via `CompositionLocal` | Part of `SamplePlatterColorScheme` struct |
| **Design tokens** | `PlatterDimens` object (spacing, radii, sizes) | `PlatterDimens` enum (matching values) |
| **Typography** | `SamplePlatterTypography()` -- Epilogue (headlines) + Manrope (body) | System fonts (SwiftUI) |
| **Theme provider** | `SamplePlatterTheme { }` composable wrapping content | `.samplePlatterTheme()` modifier at app root |

## Color System

Brand colors (red, yellow) are constant across modes. Surface and text colors adapt:

| Role | Dark | Light |
|------|------|-------|
| Background | `#131313` (Deep Obsidian) | `#FFFBFF` (Warm White) |
| Surface | `#131313` -- `#2E2E2E` (tonal grays) | `#FFFBFF` -- `#E8E0D8` (warm grays) |
| On-surface | `#FFFFFF` / `#B3B3B3` | `#1C1B1F` / `#49454F` |
| Primary | `#C1272D` (PlatterRed) | `#C1272D` (PlatterRed) |
| Secondary | `#F2B705` (PlatterYellow) | `#F2B705` (PlatterYellow) |

Extended colors (`primaryDark`, `onPrimaryButton`, `secondaryLight`, etc.) handle brand gradients and accent text that don't map to Material 3 color roles.

## Design Tokens (PlatterDimens)

Spacing, corner radii, and component sizes are centralized -- no magic numbers in UI files:

```
Spacing:  Xs(4) Sm(8) Md(12) Lg(16) Xl(24) Xxl(32) Xxxl(48)
Radii:    Sm(6) Md(12) Lg(16)
Sizes:    HeroHeight(480) CardWidth(288) CardHeight(176) IconLg(48) IconMd(40)
```

## Usage

**Kotlin** -- colors via `MaterialTheme.colorScheme.*` and `SamplePlatterTheme.extendedColors.*`, tokens via `PlatterDimens.*`:
```kotlin
Text(
    color = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.padding(PlatterDimens.SpacingXl),
)
// Extended: SamplePlatterTheme.extendedColors.primaryDark
```

**Swift** -- colors via `@Environment(\.samplePlatterColors)`, tokens via `PlatterDimens.*`:
```swift
@Environment(\.samplePlatterColors) private var colors
// ...
Text("Hello").foregroundColor(colors.onSurface)
    .padding(PlatterDimens.spacingXl)
```

## Landscape & Adaptive Layout

All screens support both portrait and landscape orientations with a "compress vertical, expand horizontal" strategy -- hero banners shrink, single-column layouts become two-column, and vertical spacing is reduced.

### Orientation Detection

| Platform | Mechanism | API |
|----------|-----------|-----|
| **Android** | `WindowSizeClass` from Material 3 | `isCompactHeight()` composable (reads `LocalWindowSizeClass`) |
| **iOS** | `verticalSizeClass` environment | `@Environment(\.verticalSizeClass)` -- landscape when `.compact` |

**Android** -- `WindowSizeClass` is calculated from the activity and provided via `CompositionLocalProvider` in `App.kt`:

```kotlin
val windowSizeClass = calculateWindowSizeClass(activity)
CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
    SamplePlatterTheme { /* ... */ }
}
```

**iOS** -- Each view reads the size class directly:

```swift
@Environment(\.verticalSizeClass) private var verticalSizeClass
private var isLandscape: Bool { verticalSizeClass == .compact }
```

### Adaptive Tokens

Dimension values that change based on orientation are centralized in `AdaptiveLayout`:

| Token | Portrait | Landscape | Used By |
|-------|----------|-----------|---------|
| `adaptiveHeroHeight` | 480 | 240 | Home hero banner |
| `adaptiveQrCodeSize` | 252/256 | 180 | Scan QR code |
| `adaptiveBottomBarPadding` | 128 | 72 | All screens (scroll padding above tab bar) |
| `adaptiveBottomNavHeight` | 80 | 56 | Bottom navigation bar |

### Per-Screen Adaptations

| Screen | Portrait | Landscape |
|--------|----------|-----------|
| **Home** | Single-column, 2-col bento grid | Compressed hero (240), 3-col bento grid |
| **Order** | Single-column featured items | 2-column featured items grid |
| **Rewards** | Stacked: points hero, vault, history | Two-column: points hero (left) + vault specials (right), then history |
| **Scan** | Stacked: QR card, progress, actions, tip | Two-column: QR card (left) + progress/actions/tip (right) |
| **Login** | Stacked: branding, form, social | Two-column: branding (left) + form/social (right) |
| **More** | Minimal change | Reduced bottom padding only |

### Testing Landscape

Both platforms include landscape-specific UI tests that verify views render correctly when orientation changes:

**Android** -- UiRobots provide a `setLandscapeContent()` method that simulates landscape via a `DpSize(800, 400)` WindowSizeClass:

```kotlin
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun setContentWith(state: HomeUiState, landscape: Boolean = false) {
    val size = if (landscape) DpSize(800.dp, 400.dp) else DpSize(400.dp, 800.dp)
    rule.setContent {
        CompositionLocalProvider(
            LocalWindowSizeClass provides WindowSizeClass.calculateFromSize(size),
        ) { SamplePlatterTheme { HomeUi(state = state) } }
    }
}
```

**iOS** -- ViewRobots create landscape views by injecting `.compact` vertical size class:

```swift
func createLandscapeView() -> some View {
    createDefaultView()
        .environment(\.verticalSizeClass, .compact)
}
```

## Key Files

```
core/theme/.../Color.kt              -- Light + dark color palettes, extended brand colors
core/theme/.../Theme.kt              -- Dual ColorScheme, CompositionLocal, SamplePlatterTheme composable
core/theme/.../PlatterDimens.kt         -- Spacing, radii, component size tokens
core/theme/.../Type.kt               -- Typography (Epilogue + Manrope font families)
core/theme/.../AdaptiveLayout.kt     -- WindowSizeClass, landscape detection, adaptive tokens (Android)
iosApp/.../Theme/SamplePlatterTheme.swift  -- iOS color scheme, dimens, environment key
iosApp/.../Theme/AdaptiveLayout.swift    -- Adaptive dimension tokens (iOS)
```
