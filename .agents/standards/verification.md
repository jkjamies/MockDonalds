# Verification Pipeline

## Full Pipeline

Run these steps in order after any code change. Stop and fix failures before proceeding.

```
 ┌─────────┐    ┌────────────┐    ┌─────────┐    ┌───────────┐    ┌───────────┐    ┌───────────┐    ┌───────────┐    ┌──────────┐
 │ 1.Detekt│───►│2.Unit Tests│───►│3.Konsist│───►│4.Harmonize│───►│5.SwiftLint│───►│6.navint   │───►│7.iOS navi │───►│8.Assemble│
 │  (lint) │    │  (Kotest)  │    │(Kt arch)│    │(iOS arch) │    │(iOS style)│    │  (emul.)  │    │  (sim.)   │    │  (build) │
 └─────────┘    └────────────┘    └─────────┘    └───────────┘    └───────────┘    └───────────┘    └───────────┘    └──────────┘
   ~15s            ~30s              ~10s            ~10s             ~5s             ~varies          ~varies          ~60s
   Fix format     Fix logic        Fix structure   Fix iOS conv.   Fix iOS style   Fix nav flows    Fix iOS nav      Fix compile
```

1. **Detekt** (lint): `./gradlew detektMetadataCommonMain`
2. **Unit tests** (Kotest): `./gradlew testAndroidHostTest`
3. **Konsist** (Kotlin architecture): `./gradlew :architecture-check:test`
4. **Harmonize** (iOS architecture): `swift test --package-path iosApp/ArchitectureCheck`
5. **SwiftLint** (Swift style): `swiftlint --config .swiftlint.yml`
6. **navint-tests** (navigation & integration, requires emulator): `./gradlew :navint-tests:connectedAndroidDeviceTest`
7. **iOS navint-tests** (iOS navigation & integration, requires simulator): `xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'`
8. **Assemble** (full build): `./gradlew assemble`

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
| `architecture-check/` | `:architecture-check:test` |
| `features/{name}/impl/presentation/` or `features/{name}/api/navigation/` | `:navint-tests:connectedAndroidDeviceTest` (requires emulator) |
| `navint-tests/` | `:navint-tests:connectedAndroidDeviceTest` (requires emulator) |
| `iosApp/iosApp/Circuit/` | `xcodebuild test -scheme iOSApp -testPlan NavIntTests ...` (requires simulator) |
| `iosApp/iosAppTests/NavInt/` | `xcodebuild test -scheme iOSApp -testPlan NavIntTests ...` (requires simulator) |

### verify-smart Decision Logic

1. Always run `:architecture-check:test` (fast, catches structural issues regardless of what changed).
2. If Kotlin source files changed: run `detektMetadataCommonMain` + scoped unit tests.
3. If Swift files changed: run `swift test --package-path iosApp/ArchitectureCheck` + `swiftlint --config .swiftlint.yml`.
4. If `features/{name}/impl/presentation/` or `features/{name}/api/navigation/` changed: run `./gradlew :navint-tests:connectedAndroidDeviceTest` (requires emulator; flag for pre-merge if emulator unavailable).
5. If `iosApp/iosApp/Circuit/` or `iosApp/iosAppTests/NavInt/` changed: run `xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'` (requires simulator; flag for pre-merge if simulator unavailable).
6. If `build.gradle.kts` or `settings.gradle.kts` changed: run `./gradlew assemble`.
6. If only markdown/documentation changed: architecture tests only (step 1).

## Failure Interpretation

### Detekt
- Reports: rule name + `file:line` (e.g., `MaxLineLength at MyFile.kt:42`)
- Auto-correct available: `./gradlew detektMetadataCommonMain --auto-correct`
- Common issues: trailing commas, import ordering, line length (120 warn / 200 error)

### Kotest (Unit Tests)
- Reports: spec name + assertion message (e.g., `HomePresenterTest - Given content loaded - Then state has items`)
- Failures indicate logic errors in implementation or test setup
- Uses BehaviorSpec Given/When/Then structure

### Konsist (Architecture Tests)
- Reports: rule name + violating class/file path
- 18 test classes in 5 categories: architecture, circuit, core, layers, testing
- Message explains the specific convention violated (e.g., "Presenters must not depend on repositories directly")

### Harmonize (iOS Architecture Tests)
- Reports: Swift convention + violating struct/class
- 29 tests enforce view conventions and test module organization
- Run via: `swift test --package-path iosApp/ArchitectureCheck`

### SwiftLint
- Reports: rule name + `file:line` (e.g., `Force Unwrapping Violation at HomeView.swift:15`)
- Auto-fix available: `swiftlint --fix --config .swiftlint.yml`
- Excludes `Circuit/` bridge code (force casts required for KMP interop)

### navint-tests (Navigation & Integration Tests)
- Reports: JUnit4 test name + assertion/exception detail
- Failures indicate broken navigation flows, incorrect Circuit presenter wiring, or missing fake setup
- Test files in `navint-tests/src/androidDeviceTest/kotlin/` end with `NavigationTest` or `IntegrationTest`
- Requires a connected Android emulator; run `./gradlew :navint-tests:connectedAndroidDeviceTest`
- These tests use real Circuit presenters and fakes — check `features/{name}/test/` for fake implementations

### iOS navint-tests (iOS Navigation & Integration Tests)
- Reports: Swift Testing suite/test name + assertion detail
- Failures indicate broken iOS navigation state management — `NavigationStateManager` logic, tab switching, deep link routing, or auth flow handling
- Test files in `iosApp/iosAppTests/NavInt/` use `@Suite @MainActor struct` (Swift Testing, not XCTest)
- Requires an iOS Simulator; run `xcodebuild test -scheme iOSApp -testPlan NavIntTests -destination 'platform=iOS Simulator,name=iPhone 16'`
- These tests verify state transitions only (no ViewInspector) — check `iosApp/iosApp/Circuit/NavigationStateManager.swift`

## Decision Tree — Skip Irrelevant Steps

```
What changed?
  │
  ├── Only Kotlin source ──► Detekt + Unit Tests + Konsist + Assemble
  │     │                    (skip Harmonize, SwiftLint)
  │     └── presentation/ or api/navigation/ changed?
  │           └── Yes ──► also run navint-tests (requires emulator)
  │
  ├── Only Swift source ──► Harmonize + SwiftLint
  │     │                    (skip Detekt, Kotest, Konsist)
  │     └── iosApp/iosApp/Circuit/ or iosApp/iosAppTests/NavInt/ changed?
  │           └── Yes ──► also run iOS navint-tests (requires simulator)
  │
  ├── Both Kotlin + Swift ──► Full pipeline (all 8 steps)
  │
  ├── Only tests changed ──► Detekt + Unit Tests + Konsist
  │     │                    (skip Assemble — tests compile as part of test tasks)
  │     └── navint-tests/ changed?
  │           └── Yes ──► run navint-tests (requires emulator)
  │
  ├── Only build.gradle.kts / settings.gradle.kts ──► Konsist + Assemble
  │
  └── Only markdown / docs ──► Konsist only
                               (AgentDocumentationTest checks AGENTS.md files)
```

Summary:
- Only Kotlin changed: skip Harmonize and SwiftLint
- Only Swift changed: skip Konsist, Detekt, and Kotlin unit tests; run iOS navint-tests if Circuit/ or NavInt/ changed
- Only tests changed: skip `./gradlew assemble` (tests compile as part of test tasks)
- Only documentation changed: run Konsist only (AgentDocumentationTest checks AGENTS.md files)
- presentation/ or api/navigation/ changed: also run navint-tests on emulator (flag for pre-merge if unavailable)
