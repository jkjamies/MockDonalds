# core:centerpost

## Purpose

The coroutine execution framework for ALL feature business logic. CenterPost wraps
coroutine launch/async with structured error handling, timeout management, and loading
state tracking. Features must never use raw `CoroutineScope.launch` or `async`.

## Public API

| Type | Description |
|------|-------------|
| `CenterPostInteractor<P, R>` | Abstract class for one-shot suspend operations. Provides `inProgress: Flow<Boolean>` loading state, configurable timeout (default 5 min), and returns `CenterPostResult<R>`. Subclass implements `doWork(params)`. |
| `CenterPostSubjectInteractor<P, T>` | Abstract class for streaming/observable operations. Emits via `flow: Flow<T>` using `flatMapLatest`. Subclass implements `createObservable(params)`. Has `collectAsState()` Compose extension. |
| `CenterPostResult<T>` | Sealed interface: `Success(data)` / `Failure(error)`. Provides `onSuccess`, `onFailure`, `map`, `flatMap`, `fold`, `getOrNull`, `getOrDefault`, `getOrElse`, `recover`. |
| `CenterPostDispatchers` | Interface abstracting `default`, `io`, `main` dispatchers. `DefaultCenterPostDispatchers` bound via `@ContributesBinding`. |
| `CenterPost` | Compose-scoped launcher created via `rememberCenterPost(dispatchers)`. Provides `invoke()` (fire-and-forget Job) and `withResult()` (Deferred of CenterPostResult). |
| `rememberCenterPost()` | `@Composable` factory that scopes a `CenterPost` to the composition. |
| `centerPostRunCatching()` | Like `runCatching` but rethrows `CancellationException` and wraps all other throwables in `CenterPostResult`. |
| `CenterPostException` | Abstract base exception for all CenterPost errors. |
| `CenterPostExecutionException` | Wraps unexpected throwables caught during execution. |
| `CenterPostTimeoutException` | Thrown when an interactor exceeds its timeout duration. |
| `CenterPostUserInitiatedParams` | Marker interface for params; controls loading debounce behavior via `isUserInitiated`. |

## Usage

### One-shot interactor in a presenter

```kotlin
class GetMenuInteractor @Inject constructor(
    private val repo: MenuRepository,
) : CenterPostInteractor<Unit, List<MenuItem>>() {
    override suspend fun doWork(params: Unit) = repo.getMenu()
}

// In presenter:
val result = getMenuInteractor(Unit)
result.onSuccess { items -> state = state.copy(menu = items) }
      .onFailure { error -> state = state.copy(error = error.message) }
```

### Streaming interactor

```kotlin
class ObserveCartInteractor @Inject constructor(
    private val repo: CartRepository,
) : CenterPostSubjectInteractor<Unit, Cart>() {
    override fun createObservable(params: Unit) = repo.cartFlow()
}
```

### Compose-scoped fire-and-forget

```kotlin
val centerPost = rememberCenterPost(dispatchers)
centerPost { repo.syncData() }
```

## Rules

- Core modules never import from features
- ALL feature business logic MUST use CenterPostInteractor or CenterPostSubjectInteractor — never raw `CoroutineScope.launch` or `async`
- **Presenters talk to the domain layer exclusively through CenterPost interactors** — inject the abstract use case from `api/domain`, use `collectAsState()` for streaming data, use `rememberCenterPost(dispatchers)` for launching one-shot operations. Presenters NEVER call repositories or domain impls directly.
- Never catch `CancellationException` — `centerPostRunCatching` handles this correctly
- Custom domain exceptions must extend `CenterPostException`
- Tests must use `TestCenterPostDispatchers` from `core:test-fixtures`
