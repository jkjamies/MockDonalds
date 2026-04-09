# Plan: Testing Infrastructure -- navint, E2E, Architecture Enforcement, Skill Fixes

## Context

The project has solid unit, presenter, and UI tests per feature, but lacks cross-feature testing. Three new test levels are needed: navigation tests (do screens transition correctly?), integration tests (does shared state propagate across features?), and E2E tests (do full user journeys work?). Additionally, the `konsist/` module should be renamed to `architecture-check/` to align with iOS's `ArchitectureCheck/` naming. Several skills have gaps around SwiftLint and iOS test coverage.

---

## Part 1: Rename `konsist/` -> `architecture-check/`

Aligns naming with iOS `iosApp/ArchitectureCheck/`.

### Changes
1. `mv konsist/ architecture-check/`
2. `settings.gradle.kts` -- change `include(":konsist")` -> `include(":architecture-check")`
3. Update all references across ~15 files:
   - `CLAUDE.md`, `README.md`, `AGENTS.md`
   - `architecture-check/AGENTS.md` (was `konsist/AGENTS.md`)
   - `.agents/standards/verification.md`, `architecture.md`, `ways-of-working.md`, `feature-scaffolding.md`, `testing.md`
   - `.agents/skills/run-arch-tests/SKILL.md`, `run-all-tests/SKILL.md`, `verify-smart/SKILL.md`, `verify/SKILL.md`
   - `.agents/AGENTS.md`

### Verification
```bash
./gradlew :testing:architecture-check:test
```

---

## Part 2: Feature Test Module DI Wiring

Add `@ContributesBinding(AppScope::class)` to all fakes so `navint-tests` can auto-discover them via Metro.

### Build changes
Each `features/*/test/build.gradle.kts`: switch plugin from `mockdonalds.kmp.library` -> `mockdonalds.kmp.domain` (adds Metro).

### Fake annotation changes
All 7 feature fakes get `@ContributesBinding(AppScope::class)` + `@Inject constructor()`:

```kotlin
@ContributesBinding(AppScope::class)
class FakeGetHomeContent @Inject constructor() : GetHomeContent() {
    private val _content = MutableStateFlow(DEFAULT)
    // ...
}
```

Files (all in `features/*/test/src/commonMain/`):
- `FakeGetHomeContent.kt`
- `FakeGetLoginContent.kt` + `FakeLoginUser.kt`
- `FakeGetOrderContent.kt`
- `FakeGetMoreContent.kt`
- `FakeGetProfileContent.kt`
- `FakeGetRewardsContent.kt`
- `FakeGetScanContent.kt`

**Why this is safe:** `composeApp` does NOT depend on `test/` modules, so fakes never appear in the production graph. Unit tests construct fakes directly -- they don't use Metro. The `@ContributesBinding` is only activated when Metro compiles a `@DependencyGraph` that has the fake on its classpath (only `navint-tests`).

### Verification
```bash
./gradlew testAndroidHostTest  # existing tests still pass
```

---

## Part 3: Android `navint-tests/` Module

Navigation + integration tests. Real Circuit, real presenters, real navigation. Fake data layer only.

### Directory structure
```
navint-tests/
  build.gradle.kts
  AGENTS.md
  src/androidDeviceTest/
    AndroidManifest.xml
    kotlin/com/mockdonalds/app/navint/
      TestAppGraph.kt
      TestApplication.kt
      TestRunner.kt
      navigation/
        HomeNavigationTest.kt        # example
      integration/
        AuthFlowIntegrationTest.kt   # example
```

### build.gradle.kts
- Android library/application with `androidDeviceTest`
- Metro plugin for `TestAppGraph`
- Auto-discover feature modules (same glob as `composeApp`):
  - `api(project(":features:$feature:api:domain"))`
  - `api(project(":features:$feature:api:navigation"))`
  - `api(project(":features:$feature:impl:presentation"))` -- real presenters + UI
  - `api(project(":features:$feature:test"))` -- fakes with `@ContributesBinding`
- Circuit dependencies: `circuit-foundation`, `circuit-runtime`, `circuit-runtime-presenter`, `circuit-runtime-ui`, `circuit-retained`, `circuitx-gesture-navigation` -- real Circuit, real navigation
- Core dependencies: `:core:circuit`, `:core:centerpost`, `:core:test-fixtures`, `:core:auth:api`, `:core:theme`, `:core:network`
- Does NOT depend on `impl/domain` or `impl/data` -- fakes are sole bindings
- Test dependencies: `compose-ui-test-junit4`, `androidx-test-runner`, `circuit-test`

### Split shared DI infrastructure

**`core:circuit`** -- keeps Screen markers, gains `CircuitProviders`. Needs Metro plugin for `@ContributesTo`/`@Provides`:
```kotlin
@ContributesTo(AppScope::class)
interface CircuitProviders {
    @Multibinds fun presenterFactories(): Set<Presenter.Factory>
    @Multibinds(allowEmpty = true) fun uiFactories(): Set<Ui.Factory>

    @Provides @SingleIn(AppScope::class)
    fun provideCircuit(presenterFactories: Set<Presenter.Factory>, uiFactories: Set<Ui.Factory>): Circuit {
        return Circuit.Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .build()
    }
}
```

**`core:metro`** -- new module, just the DI graph contract. Depends on `core:circuit` and `core:auth:api`:
```kotlin
interface AppGraph {
    val circuit: Circuit
    val authManager: AuthManager
}
```

Remove `AppGraph.kt` (graph + providers) from `composeApp`.

**`@DependencyGraph` placement -- try shared first:**

If Metro supports `@DependencyGraph` in a shared module (generating based on consumer classpath), add it directly to `AppGraph` in `core:metro`. Both `composeApp` and `navint-tests` call `createGraph<AppGraph>()`.

**Fallback if Metro requires generation at consumer module:**

`core:metro` holds the interface without the annotation. Each consuming module extends it:
```kotlin
// composeApp
@DependencyGraph(AppScope::class)
interface ProdAppGraph : AppGraph

// navint-tests
@DependencyGraph(AppScope::class)
interface NavIntAppGraph : AppGraph
```

Either way, the contract is shared. Metro generates different implementations based on each module's classpath:

| | `composeApp` | `navint-tests` |
|---|---|---|
| `api/domain` | yes | yes |
| `api/navigation` | yes | yes |
| `impl/presentation` | yes | yes |
| `impl/domain` | yes | **no** |
| `impl/data` | yes | **no** |
| `test/` | **no** | yes |

Both `composeApp` and `navint-tests` call `createGraph<AppGraph>()`. Metro generates a different implementation for each based on what's on their classpath -- same graph, same interface, different bindings:

| | `composeApp` | `navint-tests` |
|---|---|---|
| `api/domain` | yes | yes |
| `api/navigation` | yes | yes |
| `impl/presentation` | yes | yes |
| `impl/domain` | yes | **no** |
| `impl/data` | yes | **no** |
| `test/` | **no** | yes |

No `TestAppGraph`. No duplication. Scales to any number of features without changing the graph interface.

### TestApplication + TestRunner
```kotlin
class TestApplication : Application() {
    val graph: TestAppGraph by lazy { createGraph<TestAppGraph>() }
}

class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context) =
        super.newApplication(cl, TestApplication::class.java.name, context)
}
```

### AuthManager binding
`FakeAuthManager` in `core:test-fixtures` needs `@ContributesBinding(AppScope::class)` added (same pattern as feature fakes). The `core:test-fixtures` build would need Metro plugin added.

Alternative: provide `AuthManager` via `@Provides` in `TestAppGraph` to avoid modifying `core:test-fixtures`.

### AndroidManifest.xml
```xml
<manifest>
    <instrumentation
        android:name=".TestRunner"
        android:targetPackage="com.mockdonalds.app.navint" />
    <application android:name=".TestApplication">
        <activity android:name="androidx.activity.ComponentActivity" android:exported="false" />
    </application>
</manifest>
```

### Run command
```bash
./gradlew :testing:navint-tests:connectedAndroidDeviceTest
```

---

## Part 4: Android `e2e-tests/` Module

Full user journeys. Real Circuit, real navigation, real data, real everything.

### Directory structure
```
e2e-tests/
  build.gradle.kts
  AGENTS.md
  src/androidDeviceTest/
    AndroidManifest.xml
    kotlin/com/mockdonalds/app/e2e/
      robots/
        AppRobot.kt               # top-level robot: launch, navigate tabs
      benchmarks/
        StartupBenchmark.kt       # cold/warm/hot launch perf
        NavigationBenchmark.kt    # frame timing during transitions
      suites/
        GuestJourneyTest.kt       # browse -> auth wall -> login -> return
        OrderJourneyTest.kt       # home -> order -> customize
        DeepLinkJourneyTest.kt    # cold start with URI
```

### build.gradle.kts
- Depends on `:composeApp` (full real app) or instruments against `:androidApp`
- Real Metro graph (`AppGraph`), real Circuit, real navigation, real everything
- Test dependencies: `compose-ui-test-junit4`, `androidx-test-runner`
- Perfetto/Macrobenchmark dependencies for performance testing
- No feature `test/` module dependencies -- this is fully real

### Performance Testing with Perfetto
E2E tests bake in performance measurement via `androidx.benchmark.macro`:
- Startup metrics (cold/warm/hot launch times)
- Frame timing during navigation transitions
- Janky frame detection during scrolling
- Perfetto traces captured per journey for regression detection

```kotlin
@LargeTest
class StartupBenchmark : MacrobenchmarkRule() {
    @Test
    fun coldStartup() = benchmarkRule.measureRepeated(
        packageName = "com.mockdonalds.app",
        metrics = listOf(StartupTimingMetric()),
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

Journey tests can collect frame metrics alongside functional assertions -- same test, dual purpose.

### Test organization
- Kotest tags or JUnit `@Tag` per journey
- Separate `benchmarks/` directory for pure perf tests
- Run selectively: `./gradlew :testing:e2e-tests:connectedAndroidTest -Pinclude-tags=auth`

### Run command
```bash
./gradlew :testing:e2e-tests:connectedAndroidDeviceTest
```

---

## Part 5: iOS Navigation/Integration Tests

### Challenge
Navigation/integration logic lives in Kotlin presenters. Swift views are pure state renderers. The fakes live in Kotlin `test/` modules -- not directly available in Swift.

### Approach: New `iosAppNavIntTests` test target in Xcode project
- Uses Swift Testing (`@Suite`, `@Test`) -- consistent with existing `iosAppTests`
- Tests real SwiftUI navigation (`CircuitNavigator`, `NavigationStack` transitions, tab switching)
- For presenter state: create a `TestIosApp` in KMP (`composeApp/src/iosMain/`) that mirrors `IosApp` but uses `TestAppGraph` (same as Android navint) -- fakes are the sole bindings
- Export `TestIosApp` from the KMP framework so Swift tests can use it
- Tests verify: tap tab -> correct screen, deep link -> correct navigation stack, auth redirect renders login

### Key files
- `composeApp/src/iosMain/.../bridge/TestIosApp.kt` -- creates `TestAppGraph`, exposes `presenterBridge` with fakes
- `iosApp/iosAppNavIntTests/` -- Swift test target
  - `TestCircuitIos.swift` -- wraps `TestIosApp`, registers same `ScreenUiFactory` mappings
  - `Navigation/HomeNavigationTest.swift`
  - `Integration/AuthFlowIntegrationTest.swift`

### Alternative (simpler, if TestIosApp is complex)
Swift-side test data with manual state injection -- use `CircuitContent` directly with known states, test that `CircuitNavigator` handles `NavigationAction` sequences correctly. Less coverage of the Kotlin<->Swift bridge but avoids KMP test graph complexity.

---

## Part 6: iOS E2E Tests

### Approach: New `iosAppE2ETests` XCUITest target
- XCUITest provides process-isolated testing -- separate process launches real app
- Uses accessibility identifiers (shared `TestTags` from KMP) for element queries
- Organized by journey, same as Android E2E

### Structure
```
iosApp/iosAppE2ETests/
  GuestJourneyTest.swift
  OrderJourneyTest.swift
  DeepLinkJourneyTest.swift
  Robots/
    AppRobot.swift            # launch, navigate tabs, common actions
  Benchmarks/
    StartupPerformanceTest.swift
    NavigationPerformanceTest.swift
```

### Performance Testing
iOS equivalent of Perfetto -- use `XCTMetric` with `measure`:
- `XCTApplicationLaunchMetric` for startup time
- `XCTClockMetric` for duration of journeys
- `XCTMemoryMetric` for memory regression
- Xcode performance baselines for regression detection

### Key conventions
- XCUITest (NOT Swift Testing for E2E -- needs process isolation)
- All queries use `app.buttons["testTag"]` with shared TestTags
- No model construction -- interact via UI only
- Performance metrics collected alongside functional assertions where appropriate

---

## Part 7: Architecture Enforcement

Every new pattern must be enforced by Konsist (Kotlin) and Harmonize (Swift) to prevent drift.

### Konsist -- new test classes in `architecture-check/`

**`testing/NavIntModuleIsolationTest.kt`** -- classpath safety
- `composeApp` source files never import from any feature `test/` module (prevents fakes in production graph)
- `navint-tests` source files never import from `impl/domain` or `impl/data` modules (fakes must be sole bindings)
- `e2e-tests` source files never import from feature `test/` modules (E2E is fully real)

**`testing/TestModuleDITest.kt`** -- fake DI wiring
- All `Fake*` classes in `features/*/test/src/commonMain/` must have `@ContributesBinding(AppScope::class)` annotation
- All `Fake*` classes in `features/*/test/src/commonMain/` must have `@Inject` constructor annotation
- No `@ContributesBinding` in `commonTest` source sets (fakes contribute from `commonMain` only)

**`core/DependencyGraphScopeTest.kt`** -- graph placement
- `@DependencyGraph` only exists in `composeApp` and `navint-tests` (or `core:metro` if shared approach works)
- `CircuitProviders` only exists in `core:circuit`
- `AppGraph` interface only exists in `core:metro`

**`core/CoreMetroConventionsTest.kt`** -- `core:metro` isolation
- `core:metro` must not import from any feature module
- `core:metro` must not import from `impl/` modules
- `core:circuit` must not import from feature modules (existing rule, verify it covers `CircuitProviders`)

**`testing/NavIntTestNamingTest.kt`** -- test organization
- Test files in `navint-tests/navigation/` must end with `NavigationTest`
- Test files in `navint-tests/integration/` must end with `IntegrationTest`
- All navint test classes use BehaviorSpec (consistent with unit tests)

**`testing/E2ETestConventionsTest.kt`** -- E2E organization
- Test files in `e2e-tests/suites/` must end with `JourneyTest`
- Test files in `e2e-tests/benchmarks/` must end with `Benchmark`
- E2E tests must not import from feature `test/` modules (no fakes)

**Update existing `DependencyInjectionTest.kt`**
- Verify `CircuitProviders` has `@ContributesTo(AppScope::class)` in `core:circuit`
- Verify `AppGraph` exists in `core:metro`

**Update existing `LayerDependencyTest.kt`**
- `core:metro` must not depend on feature modules
- `core:circuit` must not depend on feature modules (reinforce existing rule)
- `navint-tests` must not depend on `impl/domain` or `impl/data`
- `e2e-tests` must not depend on feature `test/` modules

### Harmonize -- new/updated test files in `iosApp/ArchitectureCheck/Tests/HarmonizeTests/`

**`NavIntTestConventionsTest.swift`** (once iOS navint tests exist)
- Navigation test files end with `NavigationTest`
- Integration test files end with `IntegrationTest`
- Tests import `ComposeApp` (for shared Screen/UiState types)
- No UIKit imports (SwiftUI only)
- NavInt tests use `@Suite`/`@Test` (Swift Testing, not XCTest)
- No direct model construction outside of state setup

**`E2ETestConventionsTest.swift`** (once iOS E2E tests exist)
- E2E tests use `XCUIApplication` (process isolation required)
- All element queries use accessibility identifiers (shared TestTags, no hardcoded strings)
- No direct model construction (interact via UI only)
- Performance tests use `XCTMetric` (not custom timing)
- E2E test files organized in journey pattern (end with `JourneyTest`)
- Robot pattern: `AppRobot` exists and is used by all journey tests

**Update existing `ViewConventionsTest.swift`**
- Verify scope still excludes new test directories from production view rules

**Update existing `TestConventionsTest.swift`**
- Verify navint and E2E test directories are excluded from unit test robot pattern rules (different test pattern)

### Detekt / SwiftLint
- Detekt scope extended to cover `navint-tests/` and `e2e-tests/` source files
- SwiftLint scope in `.swiftlint.yml` extended to cover `iosAppNavIntTests/` and `iosAppE2ETests/`

---

## Part 8: Skill & Documentation Fixes

### `verify/SKILL.md` -- reorder steps
1. Build (`./gradlew assemble`)
2. Lint -- Detekt (`./gradlew detektMetadataCommonMain`)
3. Lint -- SwiftLint (`swiftlint --config .swiftlint.yml`)
4. Unit Tests (`./gradlew testAndroidHostTest`)
5. Architecture Tests -- Konsist (`./gradlew :testing:architecture-check:test`)
6. Architecture Tests -- Harmonize (`swift test --package-path iosApp/ArchitectureCheck`)

SwiftLint moves from conditional step 6 to unconditional step 3 (after Detekt).

### `verify-smart/SKILL.md` -- reorder steps
1. Detect changed files
2. Lint first (Detekt if Kotlin changed, SwiftLint if Swift changed)
3. Unit tests (scoped to changed modules)
4. Architecture tests (always -- Konsist + Harmonize)

### `run-all-tests/SKILL.md` -- add SwiftLint
1. Lint -- Detekt (`./gradlew detektMetadataCommonMain`)
2. Lint -- SwiftLint (`swiftlint --config .swiftlint.yml`)
3. Unit Tests (`./gradlew testAndroidHostTest`)
4. Architecture Tests -- Konsist (`./gradlew :testing:architecture-check:test`)
5. Architecture Tests -- Harmonize (`swift test --package-path iosApp/ArchitectureCheck`)

### `run-ui-tests/SKILL.md` -- add iOS UI tests
Add section for iOS UI tests:
```bash
# iOS UI tests (requires simulator)
xcodebuild test -project iosApp/iosApp.xcodeproj -scheme iosAppTests -destination 'platform=iOS Simulator,name=iPhone 16'
```

### `README.md` -- add SwiftLint to verification pipeline
```bash
./gradlew detektMetadataCommonMain                    # 1. Kotlin lint
swiftlint --config .swiftlint.yml                     # 2. Swift lint
./gradlew testAndroidHostTest                         # 3. Unit tests
./gradlew :testing:architecture-check:test                    # 4. Konsist
swift test --package-path iosApp/ArchitectureCheck    # 5. Harmonize
./gradlew assemble                                    # 6. Full build
```

### `CLAUDE.md` -- update verification commands
Same order as README. Update `:konsist:test` -> `:testing:architecture-check:test`. Add SwiftLint. Add new test module commands.

---

## Part 9: settings.gradle.kts

```kotlin
// Rename
include(":architecture-check")  // was :konsist

// New core module
include(":core:metro")

// New test modules
include(":navint-tests")
include(":e2e-tests")
```

---

## Part 10: AGENTS.md Updates

| File | Changes |
|------|---------|
| Root `AGENTS.md` | Add navint-tests, e2e-tests to module structure |
| `CLAUDE.md` | New test commands, updated verification pipeline |
| `architecture-check/AGENTS.md` | New test classes documented |
| `iosApp/AGENTS.md` | New test targets documented |
| `navint-tests/AGENTS.md` | New file -- purpose, TestAppGraph, test organization |
| `e2e-tests/AGENTS.md` | New file -- purpose, journey organization |
| `.agents/standards/testing.md` | New sections for navint and E2E testing |
| `.agents/standards/verification.md` | Updated commands and pipeline order |
| `.agents/standards/dependency-injection.md` | Document @ContributesBinding on fakes |

---

## Implementation Sequence

### Phase 1: Rename + Skill Fixes (no new modules)
1. Rename `konsist/` -> `architecture-check/`, update all references
2. Fix all skill files (verify, verify-smart, run-all-tests, run-ui-tests)
3. Fix README.md and CLAUDE.md verification pipeline
4. Verify: `./gradlew :testing:architecture-check:test`

### Phase 2: Feature Test Module DI Wiring
5. Switch 7 `test/` build files to `mockdonalds.kmp.domain`
6. Add `@ContributesBinding` + `@Inject constructor()` to all fakes
7. Verify: `./gradlew testAndroidHostTest` (existing tests pass)

### Phase 3: New Konsist Rules
8. Add `NavIntModuleIsolationTest`, `TestModuleDITest`, `DependencyGraphScopeTest`
9. Verify: `./gradlew :testing:architecture-check:test`

### Phase 4: Android navint-tests
10. Create module structure, build.gradle.kts, TestAppGraph, TestApplication, TestRunner
11. Add to settings.gradle.kts
12. Write first navigation + integration test
13. Create AGENTS.md
14. Verify: `./gradlew :testing:navint-tests:connectedAndroidDeviceTest`

### Phase 5: Android e2e-tests
15. Create module structure, build.gradle.kts (with Perfetto/Macrobenchmark)
16. Add to settings.gradle.kts
17. Write smoke test + startup benchmark
18. Create AGENTS.md
19. Verify: `./gradlew :testing:e2e-tests:connectedAndroidDeviceTest`

### Phase 6: iOS navint tests
20. Create `TestIosApp.kt` in `composeApp/src/iosMain/`
21. Add `iosAppNavIntTests` target to Xcode project
22. Create test infrastructure + first tests
23. Verify: `xcodebuild test -scheme iosAppNavIntTests`

### Phase 7: iOS E2E tests
24. Add `iosAppE2ETests` XCUITest target to Xcode project
25. Create AppRobot + first journey test + startup perf test
26. Verify: `xcodebuild test -scheme iosAppE2ETests`

### Phase 8: Harmonize enforcement
27. Add `NavIntTestConventionsTest.swift`, `E2ETestConventionsTest.swift`
28. Verify: `swift test --package-path iosApp/ArchitectureCheck`

### Phase 9: Documentation
29. Update all AGENTS.md and standards files
