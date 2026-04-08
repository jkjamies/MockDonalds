# MockDonalds

A Kotlin Multiplatform (KMP) reference app showcasing a clean, scalable architecture for shared business logic with native UI on both platforms: Jetpack Compose on Android and SwiftUI on iOS.

## Tech Stack

| Library | Version | Role |
|---------|---------|------|
| [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) | 2.3.20 | Shared code across Android & iOS |
| [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) | 1.10.3 | Shared Compose runtime, Android UI |
| [Circuit](https://slackhq.github.io/circuit/) | 0.33.1 | Presenter/UI/Screen pattern, navigation |
| [Metro](https://zacsweers.github.io/metro/) | 0.13.2 | Compile-time dependency injection (KMP) |
| [Molecule](https://github.com/cashapp/molecule) | 2.2.0 | Bridges `@Composable` presenters to `StateFlow` for iOS |
| [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) | 1.0.2 | Bridges Kotlin `StateFlow` to Swift `AsyncSequence` |
| [Coil](https://coil-kt.github.io/coil/) | 3.1.0 | Image loading (Compose) |
| [Ktor](https://ktor.io/) | 3.1.1 | HTTP client (KMP) |
| [Kotest](https://kotest.io/) | 6.1.11 | Test framework (BehaviorSpec, assertions, KMP) |
| [Turbine](https://github.com/cashapp/turbine) | 1.2.0 | Flow testing |
| [Circuit Test](https://slackhq.github.io/circuit/) | 0.33.1 | Presenter testing (`presenterTestOf`, `FakeNavigator`) |

## Architecture Overview

```
                        ┌─────────────────────────────────────┐
                        │            composeApp               │
                        │  AppGraph (Metro DI) + Circuit      │
                        │  Wires all feature modules together  │
                        └──────────┬──────────┬───────────────┘
                     Android       │          │          iOS
                  ┌────────────────┘          └──────────────────┐
                  │                                              │
         ┌────────▼────────┐                          ┌─────────▼─────────┐
         │   androidApp    │                          │      iosApp       │
         │   MainActivity  │                          │  SwiftUI Views    │
         │   Compose UI    │                          │  Circuit bridge   │
         └─────────────────┘                          └───────────────────┘
```

### Feature Module Architecture

Each feature follows a strict 4-layer modular architecture with unidirectional dependency flow:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Feature Module                               │
│                                                                     │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────────┐  │
│  │   api    │◄───│  domain  │◄───│   data   │    │presentation  │  │
│  │          │    │          │    │          │    │              │  │
│  │ Models   │    │ UseCase  │    │ Repo     │    │ Presenter    │  │
│  │ UseCase  │    │  Impl    │    │  Impl    │    │ UiState      │  │
│  │Interface │    │ Repo     │    │          │    │ Events       │  │
│  │ Screen   │    │Interface │    │          │    │ UI (Android) │  │
│  └──────────┘    └──────────┘    └──────────┘    └──────┬───────┘  │
│       ▲                                                 │          │
│       └─────────────────────────────────────────────────┘          │
│                    presentation depends on api only                 │
└─────────────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Convention Plugin | Contains | Depends On |
|-------|-------------------|----------|------------|
| **api** | `mockdonalds.kmp.library` | Domain models, use case interfaces, Screen definition | Nothing (pure Kotlin) |
| **domain** | `mockdonalds.kmp.domain` | Use case implementations, repository interfaces | `api` (transitive via `api()`) |
| **data** | `mockdonalds.kmp.data` | Repository implementations, data sources | `domain` (gets `api` transitively) |
| **presentation** | `mockdonalds.kmp.presentation` | Presenter, UiState, Events, Android Compose UI | `api` only (no domain/data access) |

### Dependency Flow Diagram

```
presentation ──► api ◄── domain ◄── data
                  │         │         │
                  │         │         └── Repository implementations
                  │         ├── Use case implementations
                  │         └── Repository interfaces
                  ├── Domain models
                  ├── Use case interfaces
                  └── Screen definitions
```

Key constraint: **Presentation depends only on api** — it never sees domain or data. This enforces a clean separation where presenters interact with use case interfaces, not implementations. DI wiring happens at the `composeApp` level where all modules converge.

### Dependency Injection with Metro

Metro is a compile-time DI framework for Kotlin Multiplatform. It uses a Kotlin compiler plugin (no KSP needed for DI itself).

```
┌──────────────────────────────────────────────────────────┐
│                    AppGraph (composeApp)                  │
│         @DependencyGraph(AppScope::class)                │
│                                                          │
│  Aggregates all contributed bindings from:               │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐   │
│  │  domain   │  │   data   │  │    presentation      │   │
│  │ modules   │  │ modules  │  │     modules          │   │
│  │           │  │          │  │                      │   │
│  │@Contributes│  │@Contributes│  │ @CircuitInject      │   │
│  │Binding    │  │Binding   │  │ generates factories  │   │
│  │UseCase→If │  │Repo→If   │  │ @ContributesIntoSet  │   │
│  └──────────┘  └──────────┘  └──────────────────────┘   │
│                                                          │
│  Circuit.Builder()                                       │
│    .addPresenterFactories(presenterFactories)            │
│    .addUiFactories(uiFactories)                          │
│    .build()                                              │
└──────────────────────────────────────────────────────────┘
```

#### Key DI Annotations

| Annotation | Used In | Purpose |
|------------|---------|---------|
| `@ContributesBinding(AppScope::class)` | domain, data | Binds implementation to its interface. Implies `@Inject`. |
| `@CircuitInject(Screen, AppScope)` | presentation | Generates `Presenter.Factory` / `Ui.Factory` contributed to the DI graph |
| `@Inject` | presentation | Required on `@CircuitInject` function-based presenters and UIs for Metro circuit codegen |
| `@DependencyGraph(AppScope)` | composeApp | Root DI graph that merges all contributions |
| `@ContributesTo(AppScope)` | composeApp | Contributes interface bindings (e.g., Circuit providers) |
| `@Multibinds` | composeApp | Declares set multibindings for `Presenter.Factory` and `Ui.Factory` |

### Circuit Integration

Metro 0.13.2 includes built-in Circuit codegen support, replacing the older KSP-based `circuit-codegen` processor. Enabled via:

```kotlin
metro {
    enableCircuitCodegen.set(true)
}
```

This generates `Presenter.Factory` and `Ui.Factory` implementations from `@CircuitInject`-annotated functions:

**Function-based presenter** (our pattern):
```kotlin
@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
@Composable
fun HomePresenter(
    navigator: Navigator,          // Circuit-provided (not injected)
    getHomeContent: GetHomeContent, // Injected by Metro via Provider<T>
    dispatchers: CenterPostDispatchers,
): HomeUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by getHomeContent.collectAsState()
    return HomeUiState(
        userName = content?.userName ?: "",
        eventSink = { event -> when (event) { /* ... */ } },
    )
}
```

**Function-based UI** (Android only, in `androidMain`):
```kotlin
@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
@Composable
fun HomeUi(state: HomeUiState, modifier: Modifier = Modifier) {
    // Jetpack Compose UI
}
```

Circuit-provided parameters (auto-recognized, not injected):

| Type | Available To |
|------|-------------|
| `Screen` subtypes | Presenter, UI |
| `Navigator` | Presenter only |
| `CircuitUiState` subtypes | UI only |
| `Modifier` | UI only |

All other parameters are wrapped in `Provider<T>` and injected by Metro.

#### When to Use `Lazy<T>` Injection

Parameters already wrapped in `Provider<T>`, `Lazy<T>`, or function types pass through as-is without additional wrapping. Use these when:

| Wrapper | When to Use |
|---------|-------------|
| Direct (`T`) | **Default.** Use when the dependency is always needed immediately (e.g., a use case invoked on first composition). |
| `Lazy<T>` | Use when the dependency is **conditionally needed** or **expensive to create** and may not be used on every composition. Access via `.value`. |
| `Provider<T>` | Use when you need a **new instance each time** (e.g., factory pattern). Each `.get()` call creates a new instance. |
| `() -> T` | Same as `Provider<T>`, expressed as a Kotlin function type. |

**Our convention:** All current presenters use direct injection because their use cases are always invoked immediately in composition. Prefer direct injection unless you have a clear reason for deferral.

### Data Flow

```
Android:
  Repository ──► UseCase ──► Presenter.present() ──[Compose Runtime]──► Compose UI

iOS:
  Repository ──► UseCase ──► Presenter.present() ──[Molecule]──► StateFlow
                                                    ──[KMP-NativeCoroutines]──► AsyncSequence
                                                    ──► SwiftUI View
```

### CenterPost — Business Logic Framework

CenterPost (`core/centerpost`) provides standardized patterns for use cases, coroutine launching, error handling, and loading state. Adapted from [Strata](https://github.com/jkjamies/MESA-Android).

#### Core Types

| Type | Kind | Purpose |
|------|------|---------|
| `CenterPostInteractor<P, R>` | Abstract class | One-shot use cases with timeout, error wrapping, loading state tracking |
| `CenterPostSubjectInteractor<P, T>` | Abstract class | Streaming use cases with `flatMapLatest` param switching and dedup |
| `CenterPostResult<T>` | Sealed interface | `Success<T>` / `Failure(CenterPostException)` with `map`, `flatMap`, `fold`, `recover` |
| `centerPostRunCatching` | Inline function | Safe try/catch that rethrows `CancellationException` (fixes Kotlin's `runCatching` footgun) |
| `CenterPost` | Interface | Coroutine launcher that defaults to `Dispatchers.Default` with fail-fast checks |
| `CenterPostDispatchers` | Injectable interface | Testable dispatcher provider (`default`, `io`, `main`) — injected by Metro |
| `rememberCenterPost()` | Composable function | Creates a lifecycle-bound `CenterPost` from injected `CenterPostDispatchers` |

#### CenterPost Launcher

Compose's `rememberCoroutineScope()` and `viewModelScope` both default to `Dispatchers.Main`. The `CenterPost` launcher defaults to `Dispatchers.Default` via `rememberCenterPost()`, which binds to the composition lifecycle internally:

```kotlin
@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
@Composable
fun HomePresenter(
    navigator: Navigator,
    getHomeContent: GetHomeContent,
    dispatchers: CenterPostDispatchers,     // injected by Metro, testable
): HomeUiState {
    val centerPost = rememberCenterPost(dispatchers)

    // Launches on Dispatchers.Default — not Main
    // Lifecycle-safe — cancelled when presenter leaves composition
    centerPost { /* background work */ }

    // With structured result handling
    centerPost.withResult { fetchData() }
        // returns Deferred<CenterPostResult<T>>

    // Override context if needed
    centerPost(Dispatchers.IO) { /* specific dispatcher */ }
}
```

`rememberCenterPost` handles scope and dispatcher binding: captures the composition scope via `rememberCoroutineScope()`, overrides the dispatcher to `Default` via the injected `CenterPostDispatchers`, and provides the clean `centerPost { ... }` syntax.

#### Testing with CenterPost

Swap `CenterPostDispatchers` in tests to control coroutine execution:

```kotlin
// Test fake — runs everything on the test dispatcher
class TestCenterPostDispatchers(
    testDispatcher: TestDispatcher,
) : CenterPostDispatchers {
    override val default: CoroutineDispatcher = testDispatcher
    override val main: CoroutineDispatcher = testDispatcher
}

// Presenter test using Kotest BehaviorSpec + circuit-test + molecule + turbine
class HomePresenterTest : BehaviorSpec({
    Given("a HomePresenter") {
        val testDispatcher = StandardTestDispatcher()
        val fakeGetHomeContent = FakeGetHomeContent()
        val dispatchers = TestCenterPostDispatchers(testDispatcher)

        val presenter = Presenter {
            HomePresenter(
                navigator = FakeNavigator(),
                getHomeContent = fakeGetHomeContent,
                dispatchers = TestCenterPostDispatchers(testDispatcher),
            )
        }

        When("the presenter first composes") {
            Then("it should emit an initial empty state") {
                presenter.test {
                    val state = awaitItem()
                    state.userName shouldBe ""
                    state.recentCravings shouldBe emptyList()
                }
            }
        }

        When("the use case emits content") {
            Then("it should update state with the loaded data") {
                presenter.test {
                    awaitItem() // initial empty state

                    fakeGetHomeContent.emit(testHomeContent)

                    val state = awaitItem()
                    state.userName shouldBe "Alex Mercer"
                    state.recentCravings shouldHaveSize 3
                }
            }
        }

        When("the user clicks the hero CTA") {
            Then("it should launch on the default dispatcher") {
                presenter.test {
                    fakeGetHomeContent.emit(testHomeContent)
                    val state = expectMostRecentItem()

                    state.eventSink(HomeEvent.HeroCtaClicked)
                    testDispatcher.scheduler.advanceUntilIdle()
                    // Assert navigation or side effects...
                }
            }
        }
    }
})
```

Because `rememberCenterPost` uses the injected `CenterPostDispatchers`, all coroutine launches in tests run on the `TestDispatcher` — fully synchronous and deterministic.

#### CenterPostInteractor — One-Shot Use Cases

For use cases that execute once and return a result (API calls, database writes, computations):

```kotlin
@ContributesBinding(AppScope::class)
class PlaceOrder(
    private val repository: OrderRepository,
) : CenterPostInteractor<PlaceOrderParams, OrderConfirmation>() {
    override suspend fun doWork(params: PlaceOrderParams): OrderConfirmation {
        return repository.placeOrder(params.items, params.paymentMethod)
    }
}

// Presenter usage — returns CenterPostResult<OrderConfirmation>
val result = placeOrder(PlaceOrderParams(items, payment))
result.fold(
    onSuccess = { confirmation -> /* update state */ },
    onFailure = { error -> /* show error */ },
)
```

Built-in features:
- **Timeout**: Defaults to 5 minutes, configurable per call
- **Error wrapping**: All exceptions become `CenterPostResult.Failure` (except `CancellationException` which is rethrown)
- **Loading state**: `interactor.inProgress` flow with user-initiated (immediate) vs ambient (5s debounce) tracking

#### CenterPostSubjectInteractor — Streaming Use Cases

For use cases that observe ongoing data (database queries, real-time updates):

```kotlin
@ContributesBinding(AppScope::class)
class GetHomeContentImpl(
    private val repository: HomeRepository,
) : CenterPostSubjectInteractor<Unit, HomeContent>() {
    override fun createObservable(params: Unit): Flow<HomeContent> {
        return combine(
            repository.getUserName(),
            repository.getHeroPromotion(),
            repository.getRecentCravings(),
            repository.getExploreItems(),
        ) { userName, hero, cravings, explore ->
            HomeContent(userName, hero, cravings, explore)
        }
    }
}

// Presenter usage — collectAsState() handles invoke(Unit) + flow collection
val content by interactor.collectAsState()
```

Built-in features:
- **`flatMapLatest`**: Changing params cancels the previous observable
- **`distinctUntilChanged`**: On both params (prevents re-subscription) and output (prevents duplicate emissions)

#### Exception Hierarchy

```
CenterPostException (abstract)
├── CenterPostExecutionException    — wraps unexpected Throwable
└── CenterPostTimeoutException      — wraps timeout with Duration info
```

`centerPostRunCatching` classifies exceptions:
1. `CancellationException` → **rethrown** (respects structured concurrency)
2. `CenterPostException` → captured as `Failure` (known domain errors)
3. Any other `Throwable` → wrapped in `CenterPostExecutionException`, then `Failure`

Extend `CenterPostException` for domain-specific errors (e.g., `PaymentDeclinedException`, `ItemOutOfStockException`).

#### When to Use What

| Scenario | Use |
|----------|-----|
| One-shot operation (API call, write, computation) | `CenterPostInteractor<P, R>` |
| Streaming/observable data (database, real-time) | `CenterPostSubjectInteractor<P, T>` |
| Launching background work in a presenter | `centerPost(scope) { ... }` |
| Launching with result wrapping | `centerPost.withResult(scope) { ... }` |
| Safe try/catch in any context | `centerPostRunCatching { ... }` |

### iOS Bridge Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                      Kotlin (iosMain)                            │
│                                                                  │
│  IosApp                          CircuitPresenterKotlinBridge    │
│  ├── createGraph<AppGraph>()     ├── Wraps @Composable present()│
│  ├── circuit: Circuit            ├── Molecule → StateFlow       │
│  └── presenterBridge(screen)     └── @NativeCoroutinesState     │
└──────────────────────┬───────────────────────┬───────────────────┘
                       │                       │
┌──────────────────────▼───────────────────────▼───────────────────┐
│                      Swift (iosApp)                              │
│                                                                  │
│  CircuitIos          CircuitView            CircuitContent       │
│  ├── uiFactories     ├── Observes stateFlow ├── Resolves screen │
│  ├── presenterBridge  ├── via asyncSequence  ├── Finds UI factory│
│  └── shared singleton └── Renders SwiftUI   └── Creates view    │
│                                                                  │
│  ScreenUiFactory<S, State> { view }   ← One-liner per screen   │
│  CircuitStack                         ← Navigation container    │
└──────────────────────────────────────────────────────────────────┘
```

SwiftUI views are pure functions of state — no observation logic:

```swift
struct HomeView: View {
    let state: HomeUiState
    var body: some View {
        // Pure render from state
    }
}
```

## Project Structure

```
MockDonalds/
├── androidApp/                         # Android application entry point
│   └── src/main/kotlin/
│       ├── MainActivity.kt             # Sets content to App()
│       └── MockDonaldsApplication.kt   # Application class
│
├── composeApp/                         # Shared application module
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── App.kt                  # Root Composable (CircuitCompositionLocals + NavigableCircuitContent)
│       │   ├── AppGraph.kt             # Metro DI graph + Circuit wiring
│       │   ├── MockDonaldsBottomNavigation.kt
│       │   └── MockDonaldsIcons.kt
│       └── iosMain/kotlin/
│           └── bridge/
│               ├── IosApp.kt           # Creates Metro graph, exposes presenterBridge()
│               └── CircuitPresenterKotlinBridge.kt  # Molecule + NativeCoroutines bridge
│
├── core/
│   ├── centerpost/                     # Business logic framework (interactors, result types, launcher)
│   ├── common/                         # Shared utilities (Parcelize, ResultWrapper)
│   ├── network/                        # HTTP client (Ktor)
│   ├── test-fixtures/                  # Shared test utilities (TestCenterPostDispatchers, KotestProjectConfig)
│   └── theme/                          # Design system (Colors, Typography, GlassEffect)
│
├── features/
│   ├── home/
│   │   ├── api/                        # HomeScreen, HomeModels, GetHomeContent abstract class
│   │   ├── domain/                     # GetHomeContentImpl, HomeRepository interface
│   │   ├── data/                       # HomeRepositoryImpl
│   │   ├── test/                       # Feature-specific test fakes (FakeGetHomeContent)
│   │   └── presentation/
│   │       ├── commonMain/             # HomePresenter, HomeUiState, HomeEvent
│   │       ├── androidMain/            # HomeUi (Compose) + TestTags
│   │       └── androidDeviceTest/      # UI tests: StateRobot, UiRobot, UiTest
│   ├── order/                          # Same structure: menu, featured items, cart
│   ├── rewards/                        # Same structure: points, vault specials, history
│   ├── scan/                           # Same structure: QR code, member info, progress
│   └── more/                           # Same structure: profile, menu items
│
├── iosApp/                             # iOS application (Xcode project)
│   └── iosApp/
│       ├── Circuit/                    # CircuitIos, CircuitView, CircuitContent, CircuitStack
│       ├── Theme/                      # MockDonaldsTheme (colors, fonts)
│       ├── Features/
│       │   ├── Home/HomeView.swift
│       │   ├── Order/OrderView.swift
│       │   ├── Rewards/RewardsView.swift
│       │   ├── Scan/ScanView.swift
│       │   └── More/MoreView.swift
│       ├── MockDonaldsApp.swift        # Tab bar + CircuitContent per tab
│       └── AppDelegate.swift           # UI factory registration
│
└── build-logic/convention/             # Gradle convention plugins
    └── src/main/kotlin/
        ├── mockdonalds.kmp.library.gradle.kts       # Base KMP (api modules)
        ├── mockdonalds.kmp.domain.gradle.kts        # KMP + Metro
        ├── mockdonalds.kmp.data.gradle.kts          # KMP + Metro + Serialization
        └── mockdonalds.kmp.presentation.gradle.kts  # KMP + Compose + Metro + Circuit codegen
```

## Convention Plugins

Convention plugins eliminate boilerplate across the 20+ feature modules. Each layer has a dedicated plugin:

### `mockdonalds.kmp.library`
Base KMP library plugin. Used by `api` modules. Provides:
- KMP targets: Android (`com.android.kotlin.multiplatform.library`), iOS (x64, arm64, simulator)
- Android host tests (`withHostTest { isReturnDefaultValues = true }`)
- Kotest 6.1.11 + KSP for native test discovery (`io.kotest` Gradle plugin)
- All `commonTest` dependencies: kotest-framework-engine, kotest-assertions-core, kotlinx-coroutines-test, turbine
- Auto-generated `KotestProjectConfig` per module for native KSP discovery
- `core:test-fixtures` as a `commonTest` dependency (except for test-fixtures itself)
- `kotest-runner-junit6` on `androidHostTest` with JUnit Platform configuration

### `mockdonalds.kmp.domain`
```kotlin
plugins {
    id("mockdonalds.kmp.library")
    id("dev.zacsweers.metro")           // Compile-time DI
}
```
For domain modules containing use case implementations and repository interfaces.

### `mockdonalds.kmp.data`
```kotlin
plugins {
    id("mockdonalds.kmp.library")
    id("dev.zacsweers.metro")           // Compile-time DI
    id("org.jetbrains.kotlin.plugin.serialization")  // JSON serialization
}
```
For data modules containing repository implementations and data sources.

### `mockdonalds.kmp.presentation`
```kotlin
plugins {
    id("mockdonalds.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.zacsweers.metro")
}

metro {
    enableCircuitCodegen.set(true)      // Metro's built-in Circuit codegen
}
```
For presentation modules. Provides Compose runtime, Circuit, and Metro with built-in Circuit factory generation. The `circuit-codegen-annotations` artifact is added automatically by Metro. Provides Coil for image loading on Android. Configures `androidDeviceTest` with `compose-ui-test-junit4`, `core:test-fixtures`, and `AndroidJUnitRunner` for instrumented UI tests.

## How to Add a New Feature

### 1. Create the api module

`features/{feature}/api/src/commonMain/kotlin/.../`

**Screen definition:**
```kotlin
@Parcelize
data object FeatureScreen : Screen
```

**Domain models:**
```kotlin
data class FeatureContent(
    val title: String,
    val items: List<Item>,
)
```

**Use case (abstract class extending CenterPostSubjectInteractor):**
```kotlin
abstract class GetFeatureContent : CenterPostSubjectInteractor<Unit, FeatureContent>()
```

### 2. Create the domain module

**Repository interface:**
```kotlin
interface FeatureRepository {
    fun getItems(): Flow<List<Item>>
}
```

**Use case implementation:**
```kotlin
@ContributesBinding(AppScope::class)
class GetFeatureContentImpl(
    private val repository: FeatureRepository,
) : GetFeatureContent() {
    override fun createObservable(params: Unit): Flow<FeatureContent> {
        return repository.getItems().map { FeatureContent(items = it) }
    }
}
```

`@ContributesBinding` binds this class to `GetFeatureContent` in the DI graph and implies `@Inject` — no separate `@Inject` annotation needed. Extending the abstract class enforces that all use cases go through `CenterPostSubjectInteractor`'s `flow`/`flatMapLatest`/`distinctUntilChanged` pipeline.

### 3. Create the data module

```kotlin
@ContributesBinding(AppScope::class)
class FeatureRepositoryImpl : FeatureRepository {
    override fun getItems(): Flow<List<Item>> = flowOf(/* ... */)
}
```

### 4. Create the presentation module

**UiState & Events (commonMain):**
```kotlin
data class FeatureUiState(
    val items: List<Item> = emptyList(),
    val eventSink: (FeatureEvent) -> Unit,
) : CircuitUiState

sealed interface FeatureEvent {
    data class ItemClicked(val id: String) : FeatureEvent
}
```

**Presenter (commonMain):**
```kotlin
@CircuitInject(FeatureScreen::class, AppScope::class)
@Inject
@Composable
fun FeaturePresenter(
    navigator: Navigator,
    getFeatureContent: GetFeatureContent,
    dispatchers: CenterPostDispatchers,
): FeatureUiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by getFeatureContent.collectAsState()
    return FeatureUiState(
        items = content?.items ?: emptyList(),
        eventSink = { event ->
            when (event) {
                is FeatureEvent.ItemClicked -> centerPost { navigator.goTo(DetailScreen(event.id)) }
            }
        },
    )
}
```

**Compose UI (androidMain):**
```kotlin
@CircuitInject(FeatureScreen::class, AppScope::class)
@Inject
@Composable
fun FeatureUi(state: FeatureUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        state.items.forEach { item -> Text(item.title) }
    }
}
```

### 5. Create the iOS SwiftUI view

```swift
struct FeatureView: View {
    let state: FeatureUiState
    var body: some View {
        VStack {
            ForEach(state.items, id: \.id) { item in
                Text(item.title)
            }
        }
    }
}
```

### 6. Register & wire

**AppDelegate.swift** — add one line:
```swift
ScreenUiFactory<FeatureScreen, FeatureUiState> { FeatureView(state: $0) },
```

**composeApp/build.gradle.kts** — add module dependencies:
```kotlin
// iOS framework exports:
export(project(":features:{feature}:api"))
export(project(":features:{feature}:presentation"))

// commonMain.dependencies:
api(project(":features:{feature}:api"))
implementation(project(":features:{feature}:data"))
implementation(project(":features:{feature}:domain"))
api(project(":features:{feature}:presentation"))
```

**Tab navigation** — add to `MockDonaldsApp.swift`:
```swift
CircuitContent(screen: FeatureScreen.shared)
    .tabItem { Label("Feature", systemImage: "star") }
```

### Summary: What Goes Where

| What | Where | Why |
|------|-------|-----|
| Screen | `api/commonMain` | Shared navigation contract |
| Domain Models | `api/commonMain` | Shared data types used by all layers |
| Use Case Interface | `api/commonMain` | Contract for presentation layer |
| Use Case Impl | `domain/commonMain` | Business logic, `@ContributesBinding` auto-wires |
| Repository Interface | `domain/commonMain` | Contract for data layer |
| Repository Impl | `data/commonMain` | Data source, `@ContributesBinding` auto-wires |
| Presenter | `presentation/commonMain` | Shared logic, `@CircuitInject` + `@Inject` generates factory |
| UiState/Events | `presentation/commonMain` | Shared state contract |
| Compose UI | `presentation/androidMain` | Android-only, `@CircuitInject` + `@Inject` generates factory |
| SwiftUI View | `iosApp/Features/` | iOS-only, pure state function |
| UI Registration | `AppDelegate.swift` | One-liner per screen via `ScreenUiFactory` |

## iOS Bridge Layer

The iOS bridge is a one-time setup in `composeApp/src/iosMain/`:

- **`IosApp`** — creates the Metro DI graph (`createGraph<AppGraph>()`), exposes `presenterBridge(screen:)` which resolves any screen's presenter via Circuit
- **`CircuitPresenterKotlinBridge`** — wraps a `@Composable Presenter.present()` call into a `StateFlow` via Molecule, annotated with `@NativeCoroutinesState` for Swift async observation

The Swift Circuit infrastructure (`CircuitIos`, `CircuitView`, `CircuitContent`, `CircuitStack`) consumes these bridges. `CircuitView` observes state via:

```swift
.task {
    let sequence = asyncSequence(for: presenter.stateFlow)
    for try await state in sequence { self.state = state }
}
```

Task cancellation is automatic when the view disappears.

## Building

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# iOS app — open in Xcode
open iosApp/iosApp.xcodeproj
```

The Xcode project includes a "Compile Kotlin Framework" build phase that runs the Gradle task automatically.

## Testing

### Running Tests

| Command | Scope | Speed | CI Stage |
|---------|-------|-------|----------|
| `./gradlew testAndroidHostTest` | All Android unit tests (JVM) | Fast | Unit tests |
| `./gradlew iosSimulatorArm64Test` | All iOS unit tests (K/Native) | Fast | Unit tests |
| `./gradlew :konsist:test` | Architecture enforcement only | Fast | Separate — fast feedback |
| `swift test --package-path iosApp/ArchitectureCheck` | iOS architecture enforcement (Harmonize) | Fast | Separate — fast feedback |
| `./gradlew connectedAndroidDeviceTest` | Android UI tests (emulator) | Slow | UI tests |
| `./gradlew allTests` | All targets (Android + iOS) | Medium | Full suite |
| `./gradlew detektMetadataCommonMain` | Kotlin linting (detekt + ktlint) | Fast | Lint |
| `swiftlint --config .swiftlint.yml` | Swift linting (SwiftLint) | Fast | Lint |

**Konsist runs separately from unit tests.** It is a pure JVM module (`kotlin("jvm")`) and is not included in `testAndroidHostTest`, `iosSimulatorArm64Test`, or `allTests`. This is intentional — in CI, Konsist should run as its own step for fast architectural feedback independent of the unit test suite.

Unit tests live in `commonTest` source sets and run on both Android (JVM via host tests) and iOS (native via Kotlin/Native).

### Android UI Tests (Instrumented)

Component-level UI tests run on a real device/emulator via `androidDeviceTest` (not Robolectric). They validate that Compose UI renders correctly for given states and emits correct events on interaction.

```bash
# Run all UI tests (requires a connected device or emulator)
./gradlew connectedAndroidDeviceTest

# Run a single feature's UI tests
./gradlew :features:home:presentation:connectedAndroidDeviceTest
```

#### Infrastructure

The `mockdonalds.kmp.presentation` convention plugin configures everything:

- **`withDeviceTest`** — enables the `androidDeviceTest` source set with `AndroidJUnitRunner`
- **Packaging excludes** — `META-INF/AL2.0`, `META-INF/LGPL2.1`, etc. to avoid duplicate resource conflicts
- **`compose-ui-test-junit4`** — JetBrains Compose Multiplatform test library
- **`core:test-fixtures`** — shared `StateRobot` base class
- **Disabled task** — `copyAndroidDeviceTestComposeResourcesToAndroidAssets` is disabled as a workaround for a Compose Multiplatform 1.10.3 bug

Each presentation module's `androidDeviceTest` also includes an `AndroidManifest.xml` declaring `ComponentActivity` (required by `createComposeRule()`).

#### Robot Pattern

UI tests use a two-layer robot pattern where the **UiRobot owns the StateRobot**. Tests only interact with the UiRobot:

```
Test ──► UiRobot ──► StateRobot
              │
              └──► ComposeContentTestRule
```

**StateRobot** (`core:test-fixtures`) — pure Kotlin base class for constructing `UiState` objects with captured event sinks:

```kotlin
abstract class StateRobot<State, Event> {
    val capturedEvents: List<Event>
    val lastEvent: Event?
    protected fun createEventSink(): (Event) -> Unit
    abstract fun defaultState(): State
}
```

**Feature StateRobot** — constructs states for different scenarios:

```kotlin
class HomeStateRobot : StateRobot<HomeUiState, HomeEvent>() {
    override fun defaultState() = HomeUiState(
        userName = "TestUser",
        heroPromotion = HeroPromotion(...),
        recentCravings = listOf(Craving(...)),
        exploreItems = listOf(ExploreItem(...), ExploreItem(...)),
        eventSink = createEventSink(),
    )
    fun stateWithNoPromotion() = defaultState().copy(heroPromotion = null, eventSink = createEventSink())
    fun stateWithEmptyCravings() = defaultState().copy(recentCravings = emptyList(), eventSink = createEventSink())
}
```

**UiRobot** — owns the StateRobot, wraps `ComposeContentTestRule`, exposes semantic screen assertions and actions:

```kotlin
class HomeUiRobot(private val rule: ComposeContentTestRule) {
    private val stateRobot = HomeStateRobot()

    // State + Content — always wrapped in MockDonaldsTheme
    fun setDefaultContent() {
        val state = stateRobot.defaultState()
        rule.setContent { MockDonaldsTheme { HomeUi(state = state) } }
    }

    // Screen assertions — composite checks for a given state
    fun assertDefaultScreen() {
        assertUserNameDisplayed("TestUser")
        assertHeroBannerDisplayed()
        assertRecentCravingsDisplayed()
        assertExploreSectionDisplayed()
    }

    // Actions
    fun tapHeroCtaButton() { rule.onNodeWithTag(HomeTestTags.HERO_CTA_BUTTON).performClick() }

    // Event verification
    fun assertLastEvent(expected: HomeEvent) { assertEquals(expected, stateRobot.lastEvent) }
}
```

#### Test Structure

Tests are grouped by concern — **rendering tests grouped per state** (one `setContent` call, multiple assertions), **event tests separate per interaction** (each gets its own test for clear failure attribution):

```kotlin
class HomeUiTest {
    @get:Rule val composeTestRule = createComposeRule()
    private val robot by lazy { HomeUiRobot(composeTestRule) }

    // Rendering: one test per state, composite assertion
    @Test fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test fun rendersWithNoPromotion() {
        robot.setContentWithNoPromotion()
        robot.assertScreenWithNoPromotion()
    }

    // Events: one test per interaction
    @Test fun heroCtaButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapHeroCtaButton()
        robot.assertLastEvent(HomeEvent.HeroCtaClicked)
    }
}
```

**Why this structure:** On-device `setContent` is expensive (activity launch + composition). Grouping rendering assertions per state minimizes inflations while keeping event tests isolated for clear failure messages.

#### Conventions

| Convention | Rule |
|------------|------|
| **Test tags** | `<Feature>TestTags` object in the feature's api module (`api/ui` package), shared across Android, iOS, and navigation tests |
| **Tag naming** | PascalCase: `HomeHeroBanner`, `OrderCategoryChip`. List items get ID suffix: `HomeCravingCard-{id}` |
| **Theme** | All `setContent` calls wrap content in `MockDonaldsTheme` for accurate rendering |
| **Scrolling** | Robot handles `performScrollTo()` internally — tests don't worry about viewport position |
| **File naming** | `*StateRobot.kt`, `*UiRobot.kt`, `*UiTest.kt` — all in `androidDeviceTest` source set |
| **Visibility** | Element assertions are `private` in the robot; only screen-level assertions and actions are `public` |

#### File Structure

```
features/{feature}/
├── api/src/commonMain/kotlin/.../ui/
│   └── {Feature}TestTags.kt          # Shared test tag constants (Android + iOS)
├── presentation/
│   ├── src/androidMain/kotlin/.../
│   │   └── {Feature}Ui.kt            # Compose UI (imports TestTags from api)
│   ├── src/androidDeviceTest/
│   │   ├── AndroidManifest.xml        # Declares ComponentActivity
│   │   └── kotlin/.../
│   │       ├── {Feature}StateRobot.kt # State construction with event capture
│   │       ├── {Feature}UiRobot.kt    # UI interactions + screen assertions (owns StateRobot)
│   │       └── {Feature}UiTest.kt     # JUnit4 test class
```

### Test Framework: Kotest 6.1.11

Tests use [Kotest](https://kotest.io/) BehaviorSpec (Given/When/Then) with the following stack:

| Library | Purpose |
|---------|---------|
| `kotest-framework-engine` | BehaviorSpec, test discovery (commonTest) |
| `kotest-assertions-core` | `shouldBe`, `shouldHaveSize`, etc. |
| `kotest-runner-junit6` | JUnit Platform runner (androidHostTest only) |
| `kotlinx-coroutines-test` | `TestDispatcher`, `StandardTestDispatcher` |
| `turbine` | Flow testing (`test { awaitItem() }`) |
| `circuit-test` | `presenterTestOf()`, `FakeNavigator` (presentation modules) |

All test dependencies are provided automatically by the `mockdonalds.kmp.library` convention plugin — no per-module configuration needed.

### Kotest Concurrent Spec Execution

Specs run concurrently (up to 4 in parallel) via a shared `KotestProjectConfig`:

```kotlin
// core/test-fixtures/src/commonMain/kotlin/.../KotestProjectConfig.kt
open class KotestProjectConfig : AbstractProjectConfig() {
    override val specExecutionMode = SpecExecutionMode.LimitedConcurrency(4)
}
```

**Why auto-generation is needed:** Kotest uses KSP for test discovery on native targets. KSP only processes source files within the module being compiled — it cannot discover `AbstractProjectConfig` subclasses from compiled dependencies. Following [Kotest's recommended pattern for sharing config across modules](https://kotest.io/docs/next/framework/project-config.html#sharing-config-across-modules), the convention plugin auto-generates a one-liner subclass into each module's `build/generated/kotest/commonTest/kotlin/`:

```kotlin
// Auto-generated into build/generated/ by mockdonalds.kmp.library convention plugin
package io.kotest.provided

import com.mockdonalds.app.core.test.KotestProjectConfig

class ProjectConfig : KotestProjectConfig()
```

This means:
- **One place to change settings** — edit `KotestProjectConfig` in `core:test-fixtures`
- **Zero per-module boilerplate** — the convention plugin handles generation
- **Native KSP discovers it** — because it's generated as source in each module
- **JVM discovers it** — via `systemProperty("kotest.framework.config.fqn", "io.kotest.provided.ProjectConfig")`

### Android Host Tests

The `com.android.kotlin.multiplatform.library` plugin enables Android JVM unit tests via `withHostTest`:

```kotlin
android {
    withHostTest {
        isReturnDefaultValues = true  // Prevents "Method not mocked" errors from android.util.Log etc.
    }
}
```

The `androidHostTest` source set gets `kotest-runner-junit6` for JUnit Platform integration, and all `Test` tasks are configured with `useJUnitPlatform()`. Test output includes pass/fail/skip events via `testLogging`.

### Test Module Structure

```
core/test-fixtures/                    # Shared test utilities (all modules get this automatically)
├── TestCenterPostDispatchers          # Routes all dispatchers to TestDispatcher
└── KotestProjectConfig                # Base config for concurrent spec execution

features/{feature}/test/               # Feature-specific test fakes
└── FakeGetFeatureContent              # Fake use case backed by MutableStateFlow
```

**`core:test-fixtures`** is automatically added as a `commonTest` dependency to every module by the convention plugin (except itself). It provides:
- `TestCenterPostDispatchers` — swaps all dispatchers to a `TestDispatcher` for deterministic coroutine execution
- `KotestProjectConfig` — base class for the auto-generated project config

**Feature test modules** (e.g., `features/home/test/`) contain fakes for that feature's use cases. These are `commonMain` modules (not test modules) so they can be pulled as test dependencies by other modules:

```kotlin
// features/home/test/build.gradle.kts
plugins { id("mockdonalds.kmp.library") }
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":features:home:api"))
            api(project(":core:test-fixtures"))
        }
    }
}
```

Presentation modules pull feature test fakes for presenter testing:
```kotlin
// features/home/presentation/build.gradle.kts
commonTest.dependencies {
    implementation(project(":features:home:test"))
}
```

Domain tests use inline fakes for repository interfaces (since repositories are internal to the domain/data boundary).

### Example: Presenter Test

```kotlin
class HomePresenterTest : BehaviorSpec({
    Given("a home presenter with content available") {
        val fakeGetHomeContent = FakeGetHomeContent()
        val dispatchers = TestCenterPostDispatchers()
        val navigator = FakeNavigator(HomeScreen)

        When("the presenter emits state") {
            Then("it should start with empty defaults then populate") {
                presenterTestOf(
                    presentFunction = {
                        HomePresenter(
                            navigator = navigator,
                            getHomeContent = fakeGetHomeContent,
                            dispatchers = dispatchers,
                        )
                    },
                ) {
                    val initial = awaitItem()
                    initial.userName shouldBe ""

                    val state = awaitItem()
                    state.userName shouldBe "TestUser"
                    state.heroPromotion?.title shouldBe "Test Promo"
                }
            }
        }
    }
})
```

### Example: Domain Use Case Test

```kotlin
class GetHomeContentImplTest : BehaviorSpec({
    Given("a home content interactor with repository data") {
        val userNameFlow = MutableStateFlow("TestUser")
        val repository = object : HomeRepository { /* inline fake */ }
        val interactor = GetHomeContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it should combine all repository data into HomeContent") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content.userName shouldBe "TestUser"
                }
            }
        }
    }
})
```

## Architecture Tests (Konsist)

The `:konsist` module enforces architectural conventions via [Konsist](https://docs.konsist.lemonappdev.com/) — a standalone JVM test module that scans source code without compiling against it. Run independently from unit tests:

```bash
./gradlew :konsist:test
```

### What's Enforced

| Category | Tests | What It Checks |
|----------|-------|----------------|
| **Layer Dependency** | 7 | api/domain/data/presentation isolation, cross-feature only via api, core can't import features |
| **Circuit Conventions** | 4 | Events must be `sealed class` (not interface, for iOS interop), Screens in api with `@Parcelize` |
| **Naming Conventions** | 7 | `*Screen`, `*Event`, `*Presenter`, `*Ui`, `*UiState`, `*Repository`, `*RepositoryImpl` suffixes |
| **Domain Layer** | 5 | Abstract use cases in api (CenterPostInteractor/SubjectInteractor), Impl classes in domain, Impls extend their abstract use case, matching pairs, `@ContributesBinding` |
| **Data Layer** | 6 | Repo interfaces in domain, impls in data, matching pairs, DI annotations, no `suspend` (use Flow) |
| **Presentation Layer** | 5 | `@CircuitInject` on presenters, UiState implements `CircuitUiState`, has `eventSink`, no direct model construction |
| **Api Layer** | 5 | Immutable data classes (val only), `@Serializable` placement, no public MutableFlow, DTOs in data only, circuit.runtime exports |
| **Dependency Injection** | 4 | All interfaces have `@ContributesBinding` impls, `@Inject` on presenters |
| **Visibility** | 3 | Impl classes are `internal`, UiState is public |
| **Forbidden Patterns** | 9 | No ViewModels, no raw CoroutineScope/launch/async/Dispatchers (use CenterPost), no android.* in commonMain, no app module imports |
| **Code Hygiene** | 7 | No wildcard imports, no println/System.out, no Thread.sleep, no runBlocking, no `!!` force unwraps, no lateinit var |
| **Test Conventions** | 10 | BehaviorSpec only, no mockk/runBlocking/runTest/Unconfined, fakes in test modules only, Fake coverage, Test suffix |
| **UI Test Conventions** | 9 | UiTest/UiRobot/StateRobot per feature, StateRobot extends base class, encapsulation (tests only reference UiRobot), TestTags exist in api module, theme wrapping, AndroidManifest |
| **Test Coverage** | 4 | Every feature has test module, every Impl/Presenter/Repo has a test file |
| **Package Structure** | 3 | Package naming matches module path |

### Key Architectural Rules

- **CenterPost only** — Features must use `CenterPost`/`rememberCenterPost()` for async work, not raw coroutines
- **Sealed class events** — Circuit events use `sealed class` (not `sealed interface`) so iOS Swift gets `Event.Subtype()` syntax
- **No mockk** — Thread-unsafe under `SpecExecutionMode.LimitedConcurrency(4)`, use fakes in dedicated test modules
- **Internal implementations** — All `*Impl` and `*RepositoryImpl` classes are `internal`, wired via `@ContributesBinding`
- **Cross-feature via api only** — Features can depend on other features, but only through their `:api` module

### Linting (detekt + ktlint)

Style and formatting enforcement uses [detekt](https://github.com/detekt/detekt) with the `detekt-formatting` plugin (which wraps [ktlint](https://pinterest.github.io/ktlint/) rules). Applied to all modules automatically via the `mockdonalds.detekt` convention plugin.

```bash
# Check all modules (static analysis + formatting)
./gradlew detektMetadataCommonMain

# Check Android-specific source
./gradlew detektAndroidMain

# Auto-fix formatting violations (trailing commas, import ordering, etc.)
./gradlew detektMetadataCommonMain --auto-correct
```

**Configuration:** `config/detekt/detekt.yml` — layers on top of detekt defaults (`buildUponDefaultConfig = true`).

| What It Checks | Auto-fixable |
|----------------|-------------|
| Trailing commas (call site + declaration site) | Yes |
| Import ordering | Yes |
| Max line length (120 warning, 200 error) | No — manual wrap |
| Unused private members | No |
| Wildcard imports | Yes |
| Code complexity (long methods, parameter lists) | No |
| Composable function naming (`@Composable` PascalCase allowed) | N/A |

**Convention plugin:** `mockdonalds.detekt.gradle.kts` — applied via `mockdonalds.kmp.library` so every module inherits it. Adds `detekt-formatting` plugin for ktlint rules with `autoCorrect = true`.

## Architecture Tests (Harmonize)

The `iosApp/ArchitectureCheck` SPM package enforces iOS-specific architectural conventions via [Harmonize](https://github.com/perrystreetsoftware/Harmonize) — a Swift architecture test library that uses swift-syntax for semantic AST analysis. Run independently from unit tests:

```bash
swift test --package-path iosApp/ArchitectureCheck
```

Harmonize is the iOS counterpart to Konsist. It uses `productionCode()` and `testCode()` scoping APIs to analyze Swift source files semantically (structs, classes, imports, properties, modifiers) rather than raw text.

### What's Enforced

| Category | Tests | What It Checks |
|----------|-------|----------------|
| **View Structure** | 3 | Conform to `View` protocol, import `ComposeApp` for shared KMP state, have `state` property |
| **View Safety** | 3 | No force unwraps/casts/try, no UIKit imports (pure SwiftUI), no Combine or DispatchQueue (async/await only) |
| **View Hygiene** | 2 | No `print()` statements, no `TODO`/`FIXME`/`HACK` comments |
| **View Accessibility** | 2 | Use `accessibilityIdentifier`, use shared KMP `TestTags` (not hardcoded strings) |
| **Robot Coverage** | 3 | Every `*View` has a `*ViewTest`, every `*ViewTest` has a `*ViewRobot`, every `*ViewRobot` has a `*StateRobot` |
| **Robot Structure** | 2 | `ViewRobot` classes are `final`, compose a `stateRobot` property |
| **Robot Encapsulation** | 1 | `ViewTest` files only reference `ViewRobot` — `StateRobot` is an implementation detail |
| **Robot Inheritance** | 1 | `StateRobot` classes extend `BaseStateRobot` |
| **Test Naming** | 3 | `ViewTest` classes are `final`, extend `XCTestCase`, test classes end with `Test` |
| **Test Hygiene** | 1 | No `print()` statements in test code |

### Key Architectural Rules

- **No force unwraps** — Implicitly unwrapped optionals (`Type!`), force casts (`as!`), and force try (`try!`) are banned in views; Kotlin `!!` is banned in shared production code
- **Pure SwiftUI** — Feature views must not import UIKit; all UI is SwiftUI consuming shared KMP state
- **Async/await only** — No Combine or DispatchQueue in views; use Swift concurrency
- **Shared TestTags** — Accessibility identifiers use `TestTags` constants from ComposeApp, not hardcoded strings, keeping iOS and Android test identifiers in sync
- **Robot pattern encapsulation** — `ViewTest` → `ViewRobot` → `StateRobot` layering; tests never touch `StateRobot` directly
- **Stateless views** — Every view receives its `UiState` as a `state` property from the shared Circuit presenter

### Scoping

Harmonize discovers the project root via `.harmonize.yaml` at the repo root. The architecture tests use two scopes:

- **`Harmonize.productionCode().on("iosApp/iosApp/Features")`** — View conventions (excludes test directories)
- **`Harmonize.testCode()`** — Test/robot conventions (semantic queries like `withNameEndingWith("ViewTest")` naturally filter to only the relevant classes)

### Linting (SwiftLint)

Style and formatting enforcement uses [SwiftLint](https://github.com/realm/SwiftLint). Install via Homebrew, runs from repo root.

```bash
# Install
brew install swiftlint

# Check all iOS source
swiftlint --config .swiftlint.yml

# Auto-fix (trailing whitespace, vertical whitespace, etc.)
swiftlint --fix --config .swiftlint.yml
```

**Configuration:** `.swiftlint.yml` at repo root — `included` paths scope scanning to `iosApp/iosApp` and `iosApp/iosAppTests`. Circuit bridging code is excluded (force casts required for KMP interop).

| What It Checks | Auto-fixable |
|----------------|-------------|
| Line length (120 warning, 200 error) | No — manual wrap |
| Force unwrapping, force cast, force try | No |
| Closure body length (80 warning, 160 error) | No — extract sub-views |
| Multiple closures with trailing closure | No |
| Vertical whitespace | Yes |
| Trailing whitespace | Yes |
| Implicit optional initialization | Yes |

**Xcode integration:** Add a Run Script build phase to see inline warnings:
```bash
if command -v swiftlint >/dev/null; then
  swiftlint --config "${SRCROOT}/../.swiftlint.yml"
fi
```

## Features

| Feature | Description |
|---------|-------------|
| **Home** | Greeting, hero promotional banner, recent cravings carousel, explore grid |
| **Order** | Menu categories, featured items with images, cart summary |
| **Rewards** | Points progress, vault specials gallery, transaction history |
| **Scan** | QR code display, member info, rewards progress bar |
| **More** | User profile, settings menu items |
