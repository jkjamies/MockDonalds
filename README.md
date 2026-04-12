# MockDonalds

A Kotlin Multiplatform (KMP) reference app showcasing a clean, scalable architecture for shared business logic with native UI on both platforms: Jetpack Compose on Android and SwiftUI on iOS.

## Tech Stack

| Library | Version | Role |
|---------|---------|------|
| [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) | 2.3.20 | Shared code across Android & iOS |
| [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) | 1.10.3 | Shared Compose runtime, Android UI |
| [Circuit](https://slackhq.github.io/circuit/) | 0.33.1 | Presenter/UI/Screen pattern, navigation |
| [Metro](https://zacsweers.github.io/metro/) | 0.13.2 | Compile-time dependency injection (KMP) |
| [CenterPost](core/centerpost/) | — | Business logic framework (coroutine interactors). Adapted from [Strata](https://github.com/jkjamies/MESA-Android) |
| [Molecule](https://github.com/cashapp/molecule) | 2.2.0 | Bridges `@Composable` presenters to `StateFlow` for iOS |
| [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) | 1.0.2 | Bridges Kotlin `StateFlow` to Swift `AsyncSequence` |
| [Coil](https://coil-kt.github.io/coil/) | 3.1.0 | Image loading (Compose) |
| [Ktor](https://ktor.io/) | 3.1.1 | HTTP client (KMP) |
| [BuildKonfig](https://github.com/yshrsmz/BuildKonfig) | — | Compile-time `AppBuildConfig` generation for market/env variants |
| [Kotest](https://kotest.io/) | 6.1.11 | Test framework (BehaviorSpec, assertions, KMP) |
| [Turbine](https://github.com/cashapp/turbine) | 1.2.0 | Flow testing |
| [Circuit Test](https://slackhq.github.io/circuit/) | 0.33.1 | Presenter testing (`presenterTestOf`, `FakeNavigator`) |
| [Konsist](https://docs.konsist.lemonappdev.com/) | — | Kotlin architecture test enforcement |
| [Harmonize](https://github.com/perrystreetsoftware/Harmonize) | — | Swift/iOS architecture test enforcement |

## Architecture

Shared Kotlin business logic with **native UI per platform** — Jetpack Compose on Android, SwiftUI on iOS.

### Compose Runtime, Not Compose UI (iOS)

iOS does **not** use Compose UI for rendering. The Compose _runtime_ runs presenter `@Composable` functions to produce state, then [Molecule](https://github.com/cashapp/molecule) converts that state to a `StateFlow`. SwiftUI views observe this flow via [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) and render natively. Every iOS view is standard SwiftUI — no Compose Canvas, no Compose Layout, no cross-platform UI compromise.

```
Shared:   Repository → UseCase → Presenter.present() → UiState
                                          │
              ┌───────────────────────────┤
              │                           │
Android:  Compose Runtime → Compose UI    │
                                     Molecule → StateFlow → AsyncSequence → SwiftUI  :iOS
```

See [`.agents/standards/ios-interop.md`](.agents/standards/ios-interop.md) for the full bridge architecture.

### Module Structure

Features follow a strict modular architecture:

```
features/{name}/
  api/domain/          — models, abstract use cases (public contracts)
  api/navigation/      — Screen objects, TestTags (public, Circuit-aware)
  impl/domain/         — UseCaseImpl, Repository interfaces (private)
  impl/data/           — RepositoryImpl, DTOs (private)
  impl/presentation/   — Presenter, UiState, Events, Compose UI (private)
  test/                — Fakes for testing
```

Core modules: `auth`, `build-config`, `centerpost`, `circuit`, `metro`, `network`, `theme`, `test-fixtures`

### Compile-Time Market & Environment Variants

`core:build-config` exposes a typed `AppBuildConfig` facade backed by [BuildKonfig](https://github.com/yshrsmz/BuildKonfig). Per-build inputs live in `core/build-config/src/commonMain/buildkonfig/markets/{market}.properties` (merged over `Defaults.properties`) and are selected at build time via `-Pmarket=` / `-Penv=` Gradle properties. Android's `applicationId` is derived per market (`com.mockdonalds.app.{market}`); iOS uses per-combo xcconfigs (`US-Dev`, `US-Prod`, `DE-Dev`, `DE-Prod`) driving `PRODUCT_BUNDLE_IDENTIFIER` from the `MARKET` variable.

```bash
./gradlew :androidApp:assembleDebug                                 # Default us/dev
./gradlew :androidApp:assembleRelease -Pmarket=de -Penv=prod        # Germany production build
```

See [`.agents/standards/build-config.md`](.agents/standards/build-config.md) for the full schema, Harness boundary, and "adding a field" workflow. Runtime feature flags / kill switches / experiments belong in Harness, **not** here.

Features: `home`, `login`, `more`, `order`, `profile`, `rewards`, `scan`

For detailed architecture rules, naming conventions, DI rules, forbidden patterns, and test conventions, see [`AGENTS.md`](AGENTS.md). Per-module context is in each module's `AGENTS.md` file.

## Building

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# iOS app — open in Xcode
open iosApp/iosApp.xcodeproj

# Full project build
./gradlew assemble
```

The Xcode project includes a "Compile Kotlin Framework" build phase that runs the Gradle task automatically.

## Testing

### Running Tests

| Command | Scope | Notes |
|---------|-------|-------|
| `./gradlew testAndroidHostTest` | Kotlin pure-logic unit tests (JVM host) | Fast, primary CI gate — all shared KMP logic |
| `xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'` | iOS pure-logic unit tests (Swift Testing, `iosAppTests/Unit/`) | Simulator required |
| `./gradlew connectedAndroidDeviceTest` | Android UI component tests (Compose Robot pattern) | Emulator required |
| `xcodebuild test -scheme iOSApp -testPlan UIComponentTests -destination 'platform=iOS Simulator,name=iPhone 16'` | iOS UI component tests (ViewInspector Robot, `iosAppTests/UIComponent/`) | Simulator required |
| `./gradlew :testing:navint-tests:connectedAndroidDeviceTest` | Android navigation & integration tests | Emulator required |
| `xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'` | iOS navigation & integration tests | Simulator required |
| `./gradlew :testing:e2e-tests:connectedAndroidTest` | Android E2E journey + benchmark tests | Device/emulator required |
| `xcodebuild test -scheme iOSApp -testPlan E2ETests -destination 'platform=iOS Simulator,name=iPhone 16'` | iOS E2E journey + benchmark tests | Simulator required |
| `./gradlew :testing:architecture-check:test` | Kotlin architecture (Konsist) | Host, runs separately from unit tests |
| `swift test --package-path iosApp/ArchitectureCheck` | iOS architecture (Harmonize) | Host, runs separately |
| `./gradlew :features:{name}:impl:presentation:connectedAndroidDeviceTest` | Single-feature Android UI component tests | Faster for targeted iteration |
| `./gradlew detektMetadataCommonMain` | Kotlin linting (detekt + ktlint) | `--auto-correct` for fixable violations |
| `swiftlint --config .swiftlint.yml` | Swift linting | `--fix` for auto-corrections |

### Verification Pipeline

Two shapes — full details in [`.agents/standards/verification.md`](.agents/standards/verification.md).

**Local (fast inner loop, ~2 min cold)** — lint + pure-logic unit tests + architecture + one debug build per platform. Run after every iteration:

```bash
./gradlew detektMetadataCommonMain                                                              # 1. Detekt (Kotlin lint)
swiftlint --config .swiftlint.yml                                                               # 2. SwiftLint
./gradlew testAndroidHostTest                                                                   # 3. Kotest (Kotlin unit, host)
xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'        # 4. iOS unit (Swift Testing, pure-logic)
./gradlew :testing:architecture-check:test                                                      # 5. Konsist (Kotlin arch)
swift test --package-path iosApp/ArchitectureCheck                                              # 6. Harmonize (iOS arch)
./gradlew :androidApp:assembleDebug                                                             # 7. Android debug build (one combo)
xcodebuild build -scheme iOSApp -configuration US-Dev -destination 'platform=iOS Simulator,name=iPhone 16'     # 8. iOS debug build
```

**Pre-merge / CI (thorough, ~5+ min)** — adds UI component, navint, and e2e test levels on both platforms plus a full `./gradlew assemble` across every market × env. See [`verification.md`](.agents/standards/verification.md) → "Full Pipeline (CI)" for the 13-step list. Use the `verify-ci` skill to run it locally.

### Test Framework

All tests use **Kotest BehaviorSpec** (Given/When/Then). Key libraries:

| Library | Purpose |
|---------|---------|
| `kotest-framework-engine` | BehaviorSpec, test discovery (commonTest) |
| `kotest-assertions-core` | `shouldBe`, `shouldHaveSize`, etc. |
| `kotest-runner-junit6` | JUnit Platform runner (androidHostTest only) |
| `kotlinx-coroutines-test` | `StandardTestDispatcher` for deterministic coroutines |
| `turbine` | Flow testing (`test { awaitItem() }`) |
| `circuit-test` | `presenterTestOf()`, `FakeNavigator` (presenter tests) |
| `compose-ui-test-junit4` | `createComposeRule`, `onNodeWithTag` (UI tests) |

All test dependencies are provided automatically by convention plugins — no per-module configuration.

### Test Patterns

- **Unit tests** (commonTest): Kotest BehaviorSpec + fakes + `TestCenterPostDispatchers`
- **Presenter tests**: `presenterTestOf` + `FakeNavigator` + fake use cases + Turbine
- **UI component tests** (Android, `impl/presentation/androidDeviceTest/`): Robot pattern — `UiTest` → `UiRobot` → `StateRobot`
- **UI component tests** (iOS, `iosAppTests/UIComponent/`): Swift Testing + ViewInspector — `ViewTest` → `ViewRobot` → `StateRobot`
- **iOS pure-logic unit tests** (`iosAppTests/Unit/`): Swift Testing, no ViewInspector — reserved for Swift-only helpers as iOS-side code grows
- **Architecture tests**: Konsist (Kotlin) + Harmonize (Swift) — enforce conventions statically
- **Fakes over mocks**: `mockk` is banned; fakes live in `features/{name}/test/src/commonMain/`

Specs run concurrently (up to 4 in parallel) via `KotestProjectConfig` in `core:test-fixtures`.

### Deep Link Testing

The app supports deep links via `mockdonalds://app/{path}`. Auth-gated screens (e.g. `profile`) automatically redirect to login and return after authentication.

**Android (requires emulator/device):**

```bash
# Navigate to More tab
adb shell am start -a android.intent.action.VIEW -d "mockdonalds://app/more" com.mockdonalds.app

# Navigate to Profile via More tab (auth-gated — redirects to Login if not authenticated)
adb shell am start -a android.intent.action.VIEW -d "mockdonalds://app/more/profile" com.mockdonalds.app
```

**iOS (requires simulator):**

```bash
# Navigate to More tab
xcrun simctl openurl booted "mockdonalds://app/more"

# Navigate to Profile via More tab (auth-gated — redirects to Login if not authenticated)
xcrun simctl openurl booted "mockdonalds://app/more/profile"
```

Available path segments: `home`, `order`, `rewards`, `scan`, `more`, `profile`, `login`. Segments can be chained (e.g. `more/profile`) to build a navigation stack.

## Build Profiling

```bash
# Gradle HTML report
./gradlew :androidApp:assembleDebug --profile

# Gradle Build Scan (detailed, uploads to scans.gradle.com)
./gradlew :androidApp:assembleDebug --scan

# Xcode build timing summary
xcodebuild build -project iosApp/iosApp.xcodeproj -target iosApp \
  -destination 'generic/platform=iOS Simulator' \
  -showBuildTimingSummary
```

## Features

| Feature | Description |
|---------|-------------|
| **Home** | Greeting, hero promotional banner, recent cravings carousel, explore grid |
| **Order** | Menu categories, featured items with images, cart summary |
| **Rewards** | Points progress, vault specials gallery, transaction history |
| **Scan** | QR code display, member info, rewards progress bar |
| **More** | Settings menu items, profile navigation |
| **Login** | Email sign-in, social auth (Google, Apple), return-after-auth flow |
| **Profile** | User profile (auth-gated via ProtectedScreen) |

## Agentic Automation

This codebase is fully agentic — AI agents discover conventions, scaffold features, review code, and fill test gaps via standardized files:

- **`AGENTS.md`** files in every module provide JIT context (architecture rules, naming, DI, testing standards)
- **`.agents/skills/`** directory contains 16 automation skills (verify, test, scaffold, review, config)
- **`.gemini/settings.json`** configures Gemini CLI to discover `AGENTS.md` files automatically
- **Konsist enforces** that all features, core modules, and skills have their agentic files

See [`AGENTS.md`](AGENTS.md) for the full rule set and [`.agents/AGENTS.md`](.agents/AGENTS.md) for the skill system.
