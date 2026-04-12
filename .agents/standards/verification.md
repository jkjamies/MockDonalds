# Verification Pipeline

## Local vs. CI — what each level gates

Verification comes in two shapes, and they exist for different reasons.

**Local (the `verify` skill):** prove the code you just wrote is correct and compiles on both platforms, *fast*. Lint, unit tests, architecture checks, SwiftLint, and one debug build per platform against the default market (`us-dev`). Target: under ~60s warm, under ~2 min cold. Run this on every iteration.

**CI (the full pipeline below, including `./gradlew assemble`):** prove the code ships. Every market × env combo, both platforms, both build types, release-optimized where it matters. Target: thorough, not fast. Runs once per PR.

**What `./gradlew assemble` actually does** — and why it's CI-only:

- Builds **every** Gradle module (40+) for **every** KMP target. Android (debug + release), `iosArm64`, `iosSimulatorArm64`, `iosX64` all get their own compile + link passes. Linking Kotlin/Native frameworks is single-threaded LLVM work and accounts for most of the wall time.
- Runs R8/minify on `:androidApp:assembleRelease` and the equivalent optimization passes for iOS release frameworks. These are real failure modes (obfuscation strips a reflected symbol, optimizer trips on a cinterop edge case) but they trigger in <5% of changes.
- Typically fires **~1800 tasks** on a warm cache. By contrast, `:androidApp:assembleDebug` fires ~60. The 30× task delta is the 5-minute gap.

Locally you almost never need any of that. If your change didn't touch R8 rules, Proguard keep-rules, cinterop, or `expect`/`actual` splits, debug builds prove correctness just as well as release. Pay the `assemble` cost in CI, where wall time doesn't block the dev loop.

**On iOS targets specifically:** `iosArm64` is the App Store binary, `iosSimulatorArm64` is what devs and CI run tests on, `iosX64` is the legacy Intel simulator. Local verify only needs `iosSimulatorArm64`. CI should cover `iosArm64` + `iosSimulatorArm64`. `iosX64` is vestigial — Apple has been Apple-Silicon-default since 2020 and KMP compile bugs almost never hit x64 uniquely. Drop it unless someone's actively developing on an Intel Mac.

**On the market matrix:** every market compiles the same Kotlin source; the only thing that changes is which `.properties` file gets merged into BuildKonfig. Building every combo locally proves something real (parse + merge + downstream recompile) but it's CI's job, not the dev loop's. The cheap gate that catches schema/format drift across every combo without compiling is `./gradlew :core:build-config:validateAllMarkets` (driven by the `validate-all-markets` skill) — a lightweight configuration-cache-friendly Gradle task that runs as a pre-flight check in both `verify` and `verify-ci`.

## Local (the `verify` skill)

Eight steps, symmetric across both platforms: lint → unit → architecture → debug build, plus a pre-flight market-config check. Parameterized by `market` (default `us`) and `env` (default `dev`); only the two debug build steps use those parameters. Stop and fix failures before proceeding.

**Pre-flight — `validate-all-markets`:**
```bash
./gradlew :core:build-config:validateAllMarkets
```
Gradle task on `:core:build-config`. Parses every `core/build-config/markets/*.properties` against `Defaults.properties` and enforces the rules in [build-config.md → Validation rules](build-config.md#validation-rules). Aggregates every violation in one pass and fails the build with the full list. Configuration-cache compatible; executes every invocation (no `upToDateWhen` skip) because the validation logic itself isn't an input to the task — if rules in `build.gradle.kts` change but no `.properties` file does, we still need the new rules to fire against existing files. Warm runs complete in under a second. Owned by the [`validate-all-markets`](../skills/validate-all-markets/SKILL.md) skill. Runs before step 1 because if a combo file is malformed, every downstream step builds against stale or wrong config.


```
  lint          unit          architecture    build
 ┌─────────┐   ┌─────────┐   ┌─────────┐    ┌─────────┐
 │1.Detekt │   │3.Kotest │   │5.Konsist│    │7.Android│
 │2.SwiftL.│   │4.iOSUnit│   │6.Harmon.│    │ 8.iOS   │
 └─────────┘   └─────────┘   └─────────┘    └─────────┘
   ~20s          ~60s          ~20s           ~50s
```

1. **Detekt** (Kotlin lint): `./gradlew detektMetadataCommonMain`
2. **SwiftLint** (Swift style): `swiftlint --config .swiftlint.yml`
3. **Kotest** (Kotlin pure-logic unit tests, Android host — all shared business logic runs here): `./gradlew testAndroidHostTest`
4. **iOS unit tests** (`UnitTests` test plan — Swift Testing pure-logic tests in `iosApp/iosAppTests/Unit/`, requires simulator):
   ```bash
   xcodebuild test \
     -scheme iOSApp \
     -testPlan UnitTests \
     -destination 'platform=iOS Simulator,name=iPhone 16'
   ```
   Currently a single `PlaceholderUnitTest` (`1 + 1 == 2`) — the plumbing is wired so real iOS-side pure-logic tests drop into `iosApp/iosAppTests/Unit/` without any build-system work. Most shared business logic lives in KMP Kotlin and is covered by Kotest (step 3); this slot is reserved for Swift-only helpers (e.g. `NavigationStateManager` pure-state transforms, format helpers) as iOS-only code grows.
5. **Konsist** (Kotlin architecture): `./gradlew :testing:architecture-check:test`
6. **Harmonize** (iOS architecture): `swift test --package-path iosApp/ArchitectureCheck`
7. **Android debug build** (one combo, from `market`/`env` params):
   ```bash
   ./gradlew :androidApp:assembleDebug -Pmarket=$MARKET -Penv=$ENV
   ```
   Default `us-dev`. One combo, one build type. Proves the shared Kotlin compiles for Android and the app links. No market matrix.
8. **iOS debug build** (simulator-arm64 only, from `market`/`env` params):
   ```bash
   xcodebuild build \
     -scheme iOSApp \
     -configuration ${MARKET_UPPER}-${ENV_TITLE} \
     -destination 'platform=iOS Simulator,name=iPhone 16' \
     -sdk iphonesimulator
   ```
   Simulator-arm64 only. Skips `iosArm64` (device) and `iosX64` (legacy Intel sim) — those are CI's job. Configuration name: `us` + `dev` → `US-Dev`, `de` + `prod` → `DE-Prod`.

### What the local pipeline deliberately does NOT run

- `./gradlew assemble` — 5+ min, every target × every variant on every module.
- Market matrix — every combo separately.
- Release builds — R8/minify on Android, LLVM-opt on iOS release frameworks.
- `iosArm64` (device) and `iosX64` (Intel sim).
- **UI component tests** on either platform — Android `connectedAndroidDeviceTest` (emulator) and iOS `UIComponentTests` plan (ViewInspector Robot-pattern view tests, simulator). Both run in `verify-ci`.
- navint-tests, e2e-tests — require a running device/emulator/simulator and are slower still.

### When to escalate to `verify-ci` locally

Only when the change specifically touches:
- R8/Proguard keep-rules → `./gradlew :androidApp:assembleRelease`
- `expect`/`actual` source-set splits → build both platforms explicitly
- cinterop `.def` files → build `iosArm64` too
- `core:build-config` schema, combo files, or a new market → build at least one non-default combo (`-Pmarket=de -Penv=prod`)
- Circuit navigation wiring, deep links, or presenter graph changes → also run navint-tests
- Full user journeys or startup performance work → also run e2e-tests

For anything else, the 8 steps above are enough.

## Full Pipeline (CI)

Run these steps in order after any code change. Stop and fix failures before proceeding.

Fully symmetric across both platforms, organized by concern: lint → unit → architecture → UI component → nav/int → e2e → build. Every box has one Android + one iOS step. A pre-flight `validate-all-markets` check runs before step 1.

**Pre-flight — `validate-all-markets`:** `./gradlew :core:build-config:validateAllMarkets` — see [build-config.md → Validation rules](build-config.md#validation-rules) and the [`validate-all-markets`](../skills/validate-all-markets/SKILL.md) skill. Gates the entire pipeline; fails fast if any combo file drifts from the schema.


```
  lint          unit          arch          ui-component   nav/int       e2e           build
 ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐    ┌─────────┐   ┌─────────┐   ┌─────────┐
 │1.Detekt │   │3.Kotest │   │5.Konsist│   │7.Androd │    │9.AndNvi │   │11.AndE2E│   │13.asmbl │
 │2.SwiftL.│   │4.iOSUnit│   │6.Harmon.│   │8.iOSUI  │    │10.iOSNvi│   │12.iOSE2E│   │  every  │
 └─────────┘   └─────────┘   └─────────┘   └─────────┘    └─────────┘   └─────────┘   └─────────┘
   ~20s          ~60s          ~20s         ~varies        ~varies       ~varies        ~5min
                                             (device)      (device)      (device)
```

1. **Detekt** (Kotlin lint): `./gradlew detektMetadataCommonMain`
2. **SwiftLint** (Swift style): `swiftlint --config .swiftlint.yml`
3. **Kotest** (Kotlin pure-logic unit tests, Android host — all shared logic): `./gradlew testAndroidHostTest`
4. **iOS unit tests** (`UnitTests` test plan — Swift Testing pure-logic tests in `iosApp/iosAppTests/Unit/`, requires simulator): `xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'`
5. **Konsist** (Kotlin architecture): `./gradlew :testing:architecture-check:test`
6. **Harmonize** (iOS architecture): `swift test --package-path iosApp/ArchitectureCheck`
7. **Android UI component tests** (Compose Robot pattern on per-feature presentation modules, requires emulator): `./gradlew connectedAndroidDeviceTest`
8. **iOS UI component tests** (`UIComponentTests` test plan — ViewInspector Robot-pattern view tests in `iosApp/iosAppTests/UIComponent/`, requires simulator): `xcodebuild test -scheme iOSApp -testPlan UIComponentTests -destination 'platform=iOS Simulator,name=iPhone 16'`
9. **Android navint-tests** (navigation & integration, requires emulator): `./gradlew :testing:navint-tests:connectedAndroidDeviceTest`
10. **iOS navint-tests** (`NavIntTests` test plan, requires simulator): `xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'`
11. **Android e2e-tests** (full user journeys, requires device/emulator): `./gradlew :testing:e2e-tests:connectedAndroidTest`
12. **iOS e2e-tests** (`E2ETests` test plan, requires simulator): `xcodebuild test -scheme iOSApp -testPlan E2ETests -destination 'platform=iOS Simulator,name=iPhone 16'`
13. **Assemble** (every target × every variant): `./gradlew assemble`

## Scoped Verification (verify-smart)

For targeted changes, scope checks to affected modules instead of running the full pipeline.

### Diff Detection

```bash
git diff origin/main...HEAD --name-only   # branch changes
git diff --name-only                       # uncommitted changes on main
```

### Module-to-Gradle-Task Mapping

| Path Pattern | Gradle Test Task |
|-------------|------------------|
| `features/{name}/impl/domain/` | `:features:{name}:impl:domain:testAndroidHostTest` |
| `features/{name}/impl/data/` | `:features:{name}:impl:data:testAndroidHostTest` |
| `features/{name}/impl/presentation/` | `:features:{name}:impl:presentation:testAndroidHostTest` |
| `features/{name}/api/domain/` | `:features:{name}:api:domain:testAndroidHostTest` |
| `features/{name}/test/` | Run tests for modules that consume the fakes |
| `core/{module}/` | `:core:{module}:testAndroidHostTest` |
| `features/{name}/impl/presentation/src/androidMain/` or `androidDeviceTest/` | `:features:{name}:impl:presentation:connectedAndroidDeviceTest` (Android UI component tests, requires emulator) |
| `iosApp/iosApp/Features/` | `xcodebuild test -scheme iOSApp -testPlan UIComponentTests ...` (iOS UI component tests = ViewInspector view tests, requires simulator) |
| `iosApp/iosAppTests/UIComponent/` | `xcodebuild test -scheme iOSApp -testPlan UIComponentTests ...` (iOS UI component tests = ViewInspector view tests, requires simulator) |
| `iosApp/iosAppTests/Unit/` | `xcodebuild test -scheme iOSApp -testPlan UnitTests ...` (iOS pure-logic unit tests, requires simulator) |
| `testing/architecture-check/` | `:testing:architecture-check:test` |
| `features/{name}/impl/presentation/` or `features/{name}/api/navigation/` | `:testing:navint-tests:connectedAndroidDeviceTest` (requires emulator) |
| `testing/navint-tests/` | `:testing:navint-tests:connectedAndroidDeviceTest` (requires emulator) |
| `iosApp/iosApp/Circuit/` | `xcodebuild test -scheme iOSApp -testPlan NavIntTests ...` (requires simulator) |
| `iosApp/iosAppTests/NavInt/` | `xcodebuild test -scheme iOSApp -testPlan NavIntTests ...` (requires simulator) |
| `testing/e2e-tests/` | `:testing:e2e-tests:connectedAndroidTest` (requires device/emulator) |
| `iosApp/iosAppE2ETests/` | `xcodebuild test -scheme iOSApp -testPlan E2ETests ...` (requires simulator) |

### verify-smart Decision Logic

1. Always run `:testing:architecture-check:test` (fast, catches structural issues regardless of what changed).
2. If Kotlin source files changed: run `detektMetadataCommonMain` + scoped unit tests.
3. If Swift files changed: run `swiftlint --config .swiftlint.yml` + `swift test --package-path iosApp/ArchitectureCheck`.
4. If `features/{name}/impl/presentation/src/androidMain/` or `androidDeviceTest/` changed: run `./gradlew :features:{name}:impl:presentation:connectedAndroidDeviceTest` (Android UI component tests, requires emulator; flag for pre-merge if emulator unavailable).
5. If `iosApp/iosApp/Features/` or `iosApp/iosAppTests/UIComponent/` changed: run `xcodebuild test -scheme iOSApp -testPlan UIComponentTests -destination 'platform=iOS Simulator,name=iPhone 16'` (iOS UI component tests = ViewInspector view tests, requires simulator; flag for pre-merge if simulator unavailable).
5a. If `iosApp/iosAppTests/Unit/` changed: run `xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'` (iOS pure-logic unit tests, requires simulator).
6. If `features/{name}/impl/presentation/` or `features/{name}/api/navigation/` changed: run `./gradlew :testing:navint-tests:connectedAndroidDeviceTest` (requires emulator; flag for pre-merge if emulator unavailable).
7. If `iosApp/iosApp/Circuit/` or `iosApp/iosAppTests/NavInt/` changed: run `xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'` (requires simulator; flag for pre-merge if simulator unavailable).
8. If `testing/e2e-tests/` changed: run `./gradlew :testing:e2e-tests:connectedAndroidTest` (requires device/emulator; flag for pre-merge if unavailable).
9. If `iosApp/iosAppE2ETests/` changed: run `xcodebuild test -scheme iOSApp -testPlan E2ETests -destination 'platform=iOS Simulator,name=iPhone 16'` (requires simulator; flag for pre-merge if unavailable).
10. If `build.gradle.kts` or `settings.gradle.kts` changed: run `./gradlew assemble`.
11. If only markdown/documentation changed: architecture tests only (step 1).

## Failure Interpretation

### Detekt
- Reports: rule name + `file:line` (e.g., `MaxLineLength at MyFile.kt:42`)
- Auto-correct available: `./gradlew detektMetadataCommonMain --auto-correct`
- Common issues: trailing commas, import ordering, line length (120 warn / 200 error)

### Kotest (Unit Tests)
- Reports: spec name + assertion message (e.g., `HomePresenterTest - Given content loaded - Then state has items`)
- Failures indicate logic errors in implementation or test setup
- Uses BehaviorSpec Given/When/Then structure

### iOS Unit Tests (Swift Testing, pure-logic)
- Reports: Swift Testing suite/test name + assertion detail
- Failures indicate broken Swift-side pure logic (helpers, formatters, state transforms)
- Test files live in `iosApp/iosAppTests/Unit/` and use `struct` with `@Test` functions (Swift Testing, not XCTest). No ViewInspector, no SwiftUI rendering.
- Requires an iOS Simulator; run `xcodebuild test -scheme iOSApp -testPlan UnitTests -destination 'platform=iOS Simulator,name=iPhone 16'`
- Currently holds a single `PlaceholderUnitTest` (`1 + 1 == 2`) until real Swift-side pure logic grows. Most business logic lives in KMP Kotlin and is covered by Kotest.

### iOS UI Component Tests (Swift Testing + ViewInspector)
- Reports: Swift Testing suite/test name + assertion detail
- Failures indicate broken SwiftUI view rendering, state binding, or interaction handling
- Test files in `iosApp/iosAppTests/UIComponent/` use `@Suite @MainActor struct` (Swift Testing) and the Robot pattern (`UiTest` → `UiRobot` → `StateRobot`)
- Requires an iOS Simulator; run `xcodebuild test -scheme iOSApp -testPlan UIComponentTests -destination 'platform=iOS Simulator,name=iPhone 16'`
- Uses ViewInspector to walk the SwiftUI view tree; check the matching view in `iosApp/iosApp/Features/`

### Konsist (Architecture Tests)
- Reports: rule name + violating class/file path
- 22 test classes in 5 categories: architecture, circuit, core, layers, testing
- Message explains the specific convention violated (e.g., "Presenters must not depend on repositories directly")

### Harmonize (iOS Architecture Tests)
- Reports: Swift convention + violating struct/class
- 40 tests enforce view conventions, test module organization, and E2E test conventions
- Run via: `swift test --package-path iosApp/ArchitectureCheck`

### SwiftLint
- Reports: rule name + `file:line` (e.g., `Force Unwrapping Violation at HomeView.swift:15`)
- Auto-fix available: `swiftlint --fix --config .swiftlint.yml`
- Excludes `Circuit/` bridge code (force casts required for KMP interop)

### navint-tests (Navigation & Integration Tests)
- Reports: JUnit4 test name + assertion/exception detail
- Failures indicate broken navigation flows, incorrect Circuit presenter wiring, or missing fake setup
- Test files in `testing/navint-tests/src/androidDeviceTest/kotlin/` end with `NavigationTest` or `IntegrationTest`
- Requires a connected Android emulator; run `./gradlew :testing:navint-tests:connectedAndroidDeviceTest`
- These tests use real Circuit presenters and fakes — check `features/{name}/test/` for fake implementations

### iOS navint-tests (iOS Navigation & Integration Tests)
- Reports: Swift Testing suite/test name + assertion detail
- Failures indicate broken iOS navigation state management — `NavigationStateManager` logic, tab switching, deep link routing, or auth flow handling
- Test files in `iosApp/iosAppTests/NavInt/` use `@Suite @MainActor struct` (Swift Testing, not XCTest)
- Requires an iOS Simulator; run `xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'`
- These tests verify state transitions only (no ViewInspector) — check `iosApp/iosApp/Circuit/NavigationStateManager.swift`

### e2e-tests (End-to-End Tests)
- Reports: JUnit4 test name + UI Automator assertion detail
- Failures indicate broken user journeys — screen transitions, deep link handling, tab navigation, auth gating, or startup performance regression
- Journey tests in `testing/e2e-tests/src/main/kotlin/.../suites/` end with `JourneyTest`; benchmarks in `benchmarks/` end with `Benchmark`
- Requires a connected Android device/emulator; run `./gradlew :testing:e2e-tests:connectedAndroidTest`
- Tests use UI Automator with `By.desc(testTag)` — check `AppRobot.kt` for the test helper and `features/*/api/navigation/` for TestTags
- Benchmarks use `MacrobenchmarkRule` with Perfetto traces for startup timing

### iOS e2e-tests (iOS End-to-End Tests)
- Reports: XCTest test name + XCUIElement assertion detail
- Failures indicate broken iOS user journeys — tab navigation, deep link handling, auth gating, or startup performance regression
- Journey tests in `iosApp/iosAppE2ETests/Suites/` end with `JourneyTest`; benchmarks in `Benchmarks/` end with `PerformanceTest`
- Requires an iOS Simulator; run `xcodebuild test -scheme iOSApp -testPlan E2ETests -destination 'platform=iOS Simulator,name=iPhone 16'`
- Tests use XCUITest with accessibility identifiers matching KMP TestTags (raw string constants — process-isolated)
- Uses `AppRobot` for all app interactions — check `iosApp/iosAppE2ETests/Robots/AppRobot.swift`

## IDE Troubleshooting

### Android Studio: `Cannot find 'X' in scope` on iOS build

After adding new Swift files that reference KMP types (e.g., new `ScreenUiFactory` entries in `AppDelegate.swift`, new views importing `ComposeApp`), Android Studio's iOS build may fail with `Cannot find 'X' in scope` even though `xcodebuild` from the command line succeeds. This happens because Android Studio's embedded SPM resolver has a stale cache of the ComposeApp framework.

**Fix:** Tools → Swift Package Manager → Update Dependencies. Or from the command line:
```bash
xcodebuild -resolvePackageDependencies -project iosApp/iosApp.xcodeproj -scheme iOSApp
```
This forces re-resolution of the KMP framework and picks up newly exported types.

This does not affect the `verify` pipeline (which uses `xcodebuild` directly) or Xcode (which manages its own SPM resolution).

## Decision Tree — Skip Irrelevant Steps

```
What changed?
  │
  ├── Only Kotlin source ──► Detekt + Kotest + Konsist + Android debug build
  │     │                    (skip SwiftLint, iOS unit tests, Harmonize, iOS debug build)
  │     └── presentation/ or api/navigation/ changed?
  │           └── Yes ──► also run navint-tests (requires emulator)
  │
  ├── Only Swift source ──► SwiftLint + iOS unit tests + Harmonize + iOS debug build
  │     │                    (skip Detekt, Kotest, Konsist, Android debug build)
  │     └── iosApp/iosApp/Circuit/ or iosApp/iosAppTests/NavInt/ changed?
  │           └── Yes ──► also run iOS navint-tests (requires simulator)
  │
  ├── Both Kotlin + Swift ──► Full local verify (all 8 steps)
  │
  ├── Only tests changed ──► Detekt + Kotest + Konsist (or SwiftLint + iOS unit tests + Harmonize)
  │     │                    (skip debug builds — tests compile as part of test tasks)
  │     └── testing/navint-tests/ changed?
  │           └── Yes ──► run navint-tests (requires emulator)
  │
  ├── Only build.gradle.kts / settings.gradle.kts ──► Konsist + `./gradlew assemble`
  │
  └── Only markdown / docs ──► Konsist only
                               (AgentDocumentationTest checks AGENTS.md files)
```

Summary:
- Only Kotlin changed: skip SwiftLint, iOS unit tests, Harmonize, iOS debug build
- Only Swift changed: skip Detekt, Kotest, Konsist, Android debug build; run iOS navint-tests if Circuit/ or NavInt/ changed
- Only tests changed: skip debug builds (tests compile as part of test tasks)
- Only documentation changed: run Konsist only (AgentDocumentationTest checks AGENTS.md files)
- presentation/ or api/navigation/ changed: also run navint-tests on emulator (flag for pre-merge if unavailable)
- `iosApp/iosApp/Features/` or `iosApp/iosAppTests/UIComponent/` changed: also run iOS UI component tests (requires simulator)
- `iosApp/iosAppTests/Unit/` changed: also run iOS pure-logic unit tests (requires simulator)
