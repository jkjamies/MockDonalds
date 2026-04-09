# Feature Scaffolding Reference

Step-by-step guide for adding a new feature module. Use the `add-feature` skill to automate this, or follow manually.

## What Gets Created

```
features/{name}/
├── api/
│   ├── domain/                 # Domain models, abstract use case
│   │   └── src/commonMain/kotlin/com/mockdonalds/app/features/{name}/api/domain/
│   │       ├── {Feature}Models.kt       # Data classes
│   │       └── Get{Feature}Content.kt   # abstract : CenterPostSubjectInteractor<Unit, T>()
│   └── navigation/             # Screen + TestTags
│       └── src/commonMain/kotlin/com/mockdonalds/app/features/{name}/api/navigation/
│           ├── {Feature}Screen.kt       # @Parcelize data object : Screen
│           └── ui/
│               └── {Feature}TestTags.kt # object with const val tags
├── impl/
│   ├── domain/                 # Use case impl + repository interface
│   │   └── src/commonMain/kotlin/com/mockdonalds/app/features/{name}/domain/
│   │       ├── Get{Feature}ContentImpl.kt  # @ContributesBinding : Get{Feature}Content()
│   │       └── {Feature}Repository.kt      # interface
│   ├── data/                   # Repository implementation
│   │   └── src/commonMain/kotlin/com/mockdonalds/app/features/{name}/data/
│   │       └── {Feature}RepositoryImpl.kt  # @ContributesBinding : {Feature}Repository
│   └── presentation/
│       ├── src/commonMain/kotlin/com/mockdonalds/app/features/{name}/presentation/
│       │   ├── {Feature}Presenter.kt       # @CircuitInject @Inject @Composable
│       │   ├── {Feature}UiState.kt         # data class : CircuitUiState (with eventSink)
│       │   └── {Feature}Event.kt           # sealed class
│       ├── src/androidMain/kotlin/.../
│       │   └── {Feature}Ui.kt              # @CircuitInject @Inject @Composable
│       └── src/androidDeviceTest/
│           ├── AndroidManifest.xml
│           └── kotlin/.../
│               ├── {Feature}StateRobot.kt
│               ├── {Feature}UiRobot.kt
│               └── {Feature}UiTest.kt
├── test/                       # Fakes
│   └── src/commonMain/kotlin/.../
│       └── FakeGet{Feature}Content.kt
└── AGENTS.md                   # Module context for agentic tools
```

Plus iOS files:
```
iosApp/iosApp/Features/{Feature}/{Feature}View.swift
iosApp/iosAppTests/{Feature}/
    ├── {Feature}StateRobot.swift
    ├── {Feature}ViewRobot.swift
    └── {Feature}ViewTest.swift
```

## Step-by-Step

### 1. api/domain -- Pure domain contracts (no Circuit)

```kotlin
// Models
data class {Feature}Content(
    val title: String,
    val items: List<Item>,
)

// Abstract use case
abstract class Get{Feature}Content : CenterPostSubjectInteractor<Unit, {Feature}Content>()
```

### 2. api/navigation -- Screen + TestTags

```kotlin
// Screen (singleton, @Parcelize for Circuit)
@Parcelize
data object {Feature}Screen : Screen

// TestTags (shared across Android + iOS)
object {Feature}TestTags {
    const val SECTION_A = "{Feature}SectionA"
    const val BUTTON_B = "{Feature}ButtonB"
}
```

### 3. impl/domain -- Use case impl + repository interface

```kotlin
// Repository interface (domain boundary)
interface {Feature}Repository {
    fun getItems(): Flow<List<Item>>
}

// Use case implementation (@ContributesBinding auto-wires to abstract class)
@ContributesBinding(AppScope::class)
class Get{Feature}ContentImpl(
    private val repository: {Feature}Repository,
) : Get{Feature}Content() {
    override fun createObservable(params: Unit): Flow<{Feature}Content> {
        return repository.getItems().map { {Feature}Content(items = it) }
    }
}
```

### 4. impl/data -- Repository implementation

```kotlin
@ContributesBinding(AppScope::class)
class {Feature}RepositoryImpl : {Feature}Repository {
    override fun getItems(): Flow<List<Item>> = flowOf(/* ... */)
}
```

### 5. impl/presentation -- Presenter, UiState, Events, Compose UI

**UiState & Events (commonMain):**
```kotlin
data class {Feature}UiState(
    val items: List<Item> = emptyList(),
    val eventSink: ({Feature}Event) -> Unit,
) : CircuitUiState

sealed class {Feature}Event {
    data class ItemClicked(val id: String) : {Feature}Event()
}
```

**Presenter (commonMain):**
```kotlin
@CircuitInject({Feature}Screen::class, AppScope::class)
@Inject
@Composable
fun {Feature}Presenter(
    navigator: Navigator,
    get{Feature}Content: Get{Feature}Content,
    dispatchers: CenterPostDispatchers,
): {Feature}UiState {
    val centerPost = rememberCenterPost(dispatchers)
    val content by get{Feature}Content.collectAsState()
    return {Feature}UiState(
        items = content?.items ?: emptyList(),
        eventSink = { event ->
            when (event) {
                is {Feature}Event.ItemClicked -> centerPost { navigator.goTo(DetailScreen(event.id)) }
            }
        },
    )
}
```

**Compose UI (androidMain):**
```kotlin
@CircuitInject({Feature}Screen::class, AppScope::class)
@Inject
@Composable
fun {Feature}Ui(state: {Feature}UiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        state.items.forEach { item -> Text(item.title) }
    }
}
```

### 6. iOS SwiftUI view

```swift
struct {Feature}View: View {
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    private var isLandscape: Bool { verticalSizeClass == .compact }
    let state: {Feature}UiState

    var body: some View {
        ScrollView {
            if isLandscape {
                HStack(alignment: .top, spacing: MockDimens.spacingXl) { /* ... */ }
            } else {
                VStack { /* portrait layout */ }
            }
        }
        .padding(.bottom, MockDimens.adaptiveBottomBarPadding(isLandscape: isLandscape))
    }
}
```

### 7. test/ -- Fake use case

```kotlin
class FakeGet{Feature}Content(
    initial: {Feature}Content = DEFAULT,
) : Get{Feature}Content() {
    private val _content = MutableStateFlow(initial)
    override fun createObservable(params: Unit): Flow<{Feature}Content> = _content
    fun emit(content: {Feature}Content) { _content.value = content }

    companion object {
        val DEFAULT = {Feature}Content(/* representative test data */)
    }
}
```

### 8. Register & wire

**AppDelegate.swift** -- add one line:
```swift
ScreenUiFactory<{Feature}Screen, {Feature}UiState> { {Feature}View(state: $0) },
```

**App.kt** -- add screen route mapping:
```kotlin
is {Feature}Screen -> "{feature}"
// ...
"{feature}" -> {Feature}Screen
```

**Tab navigation (MockDonaldsApp.swift)** -- if this is a tab:
```swift
CircuitContent(screen: {Feature}Screen.shared)
    .tabItem { Label("{Feature}", systemImage: "star") }
```

**settings.gradle.kts** -- AUTOMATIC (feature directory auto-discovered)
**composeApp/build.gradle.kts** -- AUTOMATIC (feature deps auto-discovered)

## What Goes Where

| What | Where | Why |
|------|-------|-----|
| Screen | `api/navigation/commonMain` | Shared navigation contract (Circuit dependency) |
| TestTags | `api/navigation/commonMain` (ui package) | Shared test tag constants (no Circuit import needed) |
| Domain Models | `api/domain/commonMain` | Shared data types -- no Circuit dependency |
| Use Case Interface | `api/domain/commonMain` | Contract for presentation layer -- no Circuit dependency |
| Use Case Impl | `impl/domain/commonMain` | Business logic, `@ContributesBinding` auto-wires |
| Repository Interface | `impl/domain/commonMain` | Contract for data layer |
| Repository Impl | `impl/data/commonMain` | Data source, `@ContributesBinding` auto-wires |
| Presenter | `impl/presentation/commonMain` | Shared logic, `@CircuitInject` + `@Inject` |
| UiState/Events | `impl/presentation/commonMain` | Shared state contract |
| Compose UI | `impl/presentation/androidMain` | Android-only, `@CircuitInject` + `@Inject` |
| SwiftUI View | `iosApp/Features/` | iOS-only, pure state function |
| UI Registration | `AppDelegate.swift` | One-liner per screen via `ScreenUiFactory` |

## Quick Checklist

1. `features/{name}/api/domain` -- Domain models, interactor abstract class (no Circuit)
2. `features/{name}/api/navigation` -- Screen (`@Parcelize`), TestTags
3. `features/{name}/impl/domain` -- Repository interface, interactor impl (`@ContributesBinding`)
4. `features/{name}/impl/data` -- Repository impl (`@ContributesBinding`)
5. `features/{name}/impl/presentation/commonMain` -- UiState/Events, Presenter (`@CircuitInject`)
6. `features/{name}/impl/presentation/androidMain` -- Compose UI (`@CircuitInject`)
7. `features/{name}/test` -- Fake interactor
8. `features/{name}/impl/presentation/commonTest` -- Presenter test (Kotest)
9. `features/{name}/impl/presentation/androidDeviceTest` -- StateRobot, UiRobot, UiTest
10. `iosApp/Features/{Name}/` -- SwiftUI view
11. `iosApp/iosAppTests/{Name}/` -- StateRobot, ViewRobot, ViewTest (Swift Testing)
12. `App.kt` -- screen route mapping
13. `AppDelegate.swift` -- `ScreenUiFactory` registration
14. Verify -- detekt, unit tests, architecture-check, harmonize, swiftlint, assemble
