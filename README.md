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
| [Kotest](https://kotest.io/) | 6.1.11 | Test framework (BehaviorSpec, assertions, KMP) |
| [Turbine](https://github.com/cashapp/turbine) | 1.2.0 | Flow testing |
| [Circuit Test](https://slackhq.github.io/circuit/) | 0.33.1 | Presenter testing (`presenterTestOf`, `FakeNavigator`) |
| [Konsist](https://docs.konsist.lemonappdev.com/) | — | Kotlin architecture test enforcement |
| [Harmonize](https://github.com/perrystreetsoftware/Harmonize) | — | Swift/iOS architecture test enforcement |

## Architecture

Shared Kotlin business logic with native UI per platform. Features follow a strict modular architecture:

```
features/{name}/
  api/domain/          — models, abstract use cases (public contracts)
  api/navigation/      — Screen objects, TestTags (public, Circuit-aware)
  impl/domain/         — UseCaseImpl, Repository interfaces (private)
  impl/data/           — RepositoryImpl, DTOs (private)
  impl/presentation/   — Presenter, UiState, Events, Compose UI (private)
  test/                — Fakes for testing
```

Core modules: `auth`, `centerpost`, `circuit`, `network`, `theme`, `test-fixtures`

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
| `./gradlew testAndroidHostTest` | All Android unit tests (JVM) | Fast, primary CI gate |
| `./gradlew iosSimulatorArm64Test` | All iOS unit tests (K/Native) | Fast |
| `./gradlew :konsist:test` | Architecture enforcement (Konsist) | Runs separately from unit tests |
| `swift test --package-path iosApp/ArchitectureCheck` | iOS architecture (Harmonize) | Runs separately |
| `./gradlew connectedAndroidDeviceTest` | Android UI tests (emulator) | Requires device/emulator |
| `./gradlew :features:{name}:impl:presentation:connectedAndroidDeviceTest` | Single feature UI tests | Faster for targeted testing |
| `./gradlew allTests` | All targets (Android + iOS) | Full suite |
| `./gradlew detektMetadataCommonMain` | Kotlin linting (detekt + ktlint) | `--auto-correct` for fixable violations |
| `swiftlint --config .swiftlint.yml` | Swift linting | `--fix` for auto-corrections |

### Verification Pipeline

After any code change, run in order:

```bash
./gradlew detektMetadataCommonMain          # 1. Lint
./gradlew testAndroidHostTest               # 2. Unit tests
./gradlew :konsist:test                     # 3. Architecture enforcement
swift test --package-path iosApp/ArchitectureCheck  # 4. iOS arch (if Swift changed)
./gradlew assemble                          # 5. Full build
```

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
- **UI tests** (androidDeviceTest): Robot pattern — `UiTest` → `UiRobot` → `StateRobot`
- **iOS view tests** (iosAppTests): Swift Testing + ViewInspector — `ViewTest` → `ViewRobot` → `StateRobot`
- **Architecture tests**: Konsist (Kotlin) + Harmonize (Swift) — enforce conventions statically
- **Fakes over mocks**: `mockk` is banned; fakes live in `features/{name}/test/src/commonMain/`

Specs run concurrently (up to 4 in parallel) via `KotestProjectConfig` in `core:test-fixtures`.

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
- **`.agents/skills/`** directory contains 14 automation skills (verify, test, scaffold, review)
- **`.gemini/settings.json`** configures Gemini CLI to discover `AGENTS.md` files automatically
- **Konsist enforces** that all features, core modules, and skills have their agentic files

See [`AGENTS.md`](AGENTS.md) for the full rule set and [`.agents/AGENTS.md`](.agents/AGENTS.md) for the skill system.
