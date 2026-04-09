# Code Style

## Detekt Configuration

Config file: `config/detekt/detekt.yml`
Applied via: `mockdonalds.detekt` convention plugin (transitively through `mockdonalds.kmp.library`)
Mode: `buildUponDefaultConfig = true` — only overrides rules that need tuning

### Key Detekt Rules

| Rule | Setting |
|------|---------|
| TrailingCommaOnCallSite | Required (`useTrailingCommaOnCallSite: true`) |
| TrailingCommaOnDeclarationSite | Required (`useTrailingCommaOnDeclarationSite: true`) |
| ImportOrdering | Active (enforced by detekt-formatting / ktlint) |
| MaxLineLength | 120 warn (comments excluded) |
| WildcardImport | Forbidden (also enforced by Konsist CodeHygieneTest) |
| UnusedImports | Active |
| UnusedPrivateMember | Active |
| MagicNumber | Disabled (too noisy for UI padding/size values) |
| LongMethod | Threshold 300 (Compose UI functions are declarative and long) |
| LongParameterList | Function: 8, Constructor: 10 |
| TooManyFunctions | File: 20, Class: 15 |
| UnusedParameter | Excluded for `*Presenter.kt` (Circuit requires navigator param) |
| TooGenericExceptionCaught | Excluded for `**/centerpost/**` (intentional error wrapping) |

### detekt-formatting (ktlint Rules)

- `autoCorrect = true` — formatting issues are auto-fixed when `--auto-correct` is passed
- Provided by the detekt-formatting plugin, configured in `mockdonalds.detekt.gradle.kts`
- MaximumLineLength disabled in formatting block (defers to style.MaxLineLength to avoid double-reporting)

### Compose-Specific Exception

`FunctionNaming` ignores `@Composable` annotated functions, allowing PascalCase naming
(e.g., `HomeUi`, `MockDonaldsBottomNavigation`) per Compose conventions.

## Auto-Fix Commands (Kotlin)

```bash
./gradlew detektMetadataCommonMain --auto-correct
```

## SwiftLint Configuration

Config file: `.swiftlint.yml` at repo root
Included paths: `iosApp/iosApp`, `iosApp/iosAppTests`
Excluded paths: `iosApp/iosApp/Circuit` (KMP bridge code requires force casts), `iosApp/ArchitectureCheck/.build`

### Key SwiftLint Rules

| Rule | Setting |
|------|---------|
| force_unwrapping | Opt-in, active (error) |
| force_cast | Opt-in, active (error) |
| force_try | Opt-in, active (error) |
| line_length | 120 warn / 200 error |
| closure_body_length | 80 warn / 160 error |
| function_body_length | 60 warn / 100 error |
| type_body_length | 300 warn / 500 error |
| vertical_whitespace_closing_braces | Opt-in, active |
| implicit_optional_initialization | Opt-in, active |

### Disabled SwiftLint Rules

- `todo` — disabled because Harmonize enforces no TODO/FIXME/HACK in views (more targeted)
- `trailing_comma` — disabled (Swift convention does not use trailing commas)

## Auto-Fix Commands (Swift)

```bash
swiftlint --fix --config .swiftlint.yml
```
