# core:theme

## Purpose

The design system for MockDonalds -- provides Material 3 theming, brand colors,
typography, spacing tokens, adaptive layout utilities, and glass-morphism effects.
Used in ALL UI code and ALL UI test robots.

## Public API

| Type | Description |
|------|-------------|
| `MockDonaldsTheme` | `@Composable` function wrapping `MaterialTheme` with brand color schemes (dark/light) and custom typography. Also an `object` exposing `extendedColors`. |
| `MockDonaldsExtendedColors` | `@Immutable` data class with brand accent colors not in M3: `primaryDark`, `primaryDarker`, `onPrimaryButton`, `onSecondaryTag`, `onSecondaryContainer`, `secondaryLight`. |
| `LocalMockDonaldsExtendedColors` | `staticCompositionLocalOf` providing `MockDonaldsExtendedColors` to the tree. |
| `MockDimens` | Object with spacing scale (`SpacingXs`..`SpacingXxxl`), corner radii (`RadiusSm`/`Md`/`Lg`), and component sizes (`HeroHeight`, `CardWidth`, `BottomNavHeight`, etc.). |
| `MockDonaldsTypography()` | `@Composable` function returning M3 `Typography` using Epilogue (headlines/titles) and Manrope (body/labels) font families. |
| `EpilogueFamily` / `ManropeFamily` | `@Composable` `FontFamily` properties loading custom fonts from resources. |
| `LocalWindowSizeClass` | `compositionLocalOf<WindowSizeClass>` for responsive layout. Must be provided in `App.kt`. |
| `isCompactHeight()` / `isExpandedWidth()` | `@Composable` helpers reading `LocalWindowSizeClass`. |
| `adaptiveHeroHeight()` / `adaptiveQrCodeSize()` / `adaptiveBottomBarPadding()` / `adaptiveBottomNavHeight()` | `@Composable` functions returning responsive `Dp` values. |
| `Modifier.glassEffect()` | Glass morphism modifier with translucent surface overlay. |
| `Modifier.ambientGradient()` | Vertical gradient used in place of elevation borders. |
| `MockDonaldsBrandGradient` | Horizontal `Brush` from `MockRed` to `MockYellow`. |
| Brand colors | `MockRed`, `MockYellow`, `DeepObsidian`, plus full dark/light surface and status color tokens. |

## Usage

### Wrapping a screen

```kotlin
MockDonaldsTheme {
    MenuScreen(state, eventSink)
}
```

### Accessing extended colors

```kotlin
val colors = MockDonaldsTheme.extendedColors
Text(color = colors.primaryDark, ...)
```

### Using spacing tokens

```kotlin
Modifier.padding(MockDimens.SpacingLg)
```

### Adaptive layout

```kotlin
val heroHeight = adaptiveHeroHeight()
```

## Rules

- Core modules never import from features
- ALL UI composables must be wrapped in `MockDonaldsTheme`
- Use `MockDimens` spacing tokens instead of raw `dp` literals
- Use `MaterialTheme.colorScheme` for M3 colors, `MockDonaldsTheme.extendedColors` for brand accents
- Use adaptive layout helpers for dimension-sensitive UI to support compact/expanded layouts
- UI test robots must also wrap content in `MockDonaldsTheme`
- No elevation borders -- use `glassEffect` or `ambientGradient` for depth
