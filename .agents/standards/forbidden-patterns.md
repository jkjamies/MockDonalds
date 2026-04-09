# Forbidden Patterns

Every banned pattern in the MockDonalds codebase with rationale, alternative, and enforcing Konsist test.

## Code Hygiene

### Wildcard Imports

- **Banned:** `import com.example.*`
- **Why:** Hurts readability (unclear what is used), causes merge conflicts when two branches add different symbols from the same package, can introduce ambiguous references.
- **Instead:** Use explicit imports for each symbol.
- **Enforced by:** `CodeHygieneTest` -- "production code should not use wildcard imports"

### println / System.out / System.err

- **Banned:** `println(...)`, `System.out.println(...)`, `System.err.println(...)`
- **Why:** Debug logging leaks to production, provides no log levels or structured context, not appropriate for a KMP codebase.
- **Instead:** Use a structured logging framework.
- **Enforced by:** `CodeHygieneTest` -- "production code should not use println", "production code should not use System.out or System.err"

### Thread.sleep

- **Banned:** `Thread.sleep(...)` in all code (production AND test)
- **Why:** Blocks the thread, wastes resources, is non-deterministic in tests, defeats coroutine-based concurrency.
- **Instead:** Use `delay()` in coroutines, or `advanceTimeBy()` / `advanceUntilIdle()` in tests.
- **Enforced by:** `CodeHygieneTest` -- "no Thread.sleep in production or test code"

### runBlocking

- **Banned:** `runBlocking { }` in production code
- **Why:** Blocks the calling thread, can cause deadlocks on Main dispatcher, defeats the purpose of coroutines.
- **Instead:** Use `suspend` functions, or `CenterPost` / `rememberCenterPost()` for presenter-scoped launching.
- **Enforced by:** `CodeHygieneTest` -- "no runBlocking in production code"

### Non-null Assertion (!!)

- **Banned:** `!!` operator in production code
- **Why:** Crashes at runtime with `NullPointerException` instead of handling nullability gracefully.
- **Instead:** Use safe calls (`?.`), `let { }`, `checkNotNull()` with descriptive message, `requireNotNull()`, `Elvis (?:)`, or redesign to avoid nullability.
- **Enforced by:** `CodeHygieneTest` -- "production code should not use non-null assertion operator"

### lateinit var

- **Banned:** `lateinit var` in shared production code (commonMain, androidMain)
- **Why:** KMP compatibility issues across platforms, can cause `UninitializedPropertyAccessException`, indicates missing constructor injection.
- **Instead:** Use constructor injection, `lazy { }`, or nullable with default.
- **Enforced by:** `CodeHygieneTest` -- "production classes should not use lateinit var"

## Architecture

### ViewModel / AndroidViewModel

- **Banned:** Extending `ViewModel` or `AndroidViewModel`, importing `androidx.lifecycle.ViewModel`
- **Why:** This project uses Circuit presenters for state management. ViewModels are Android-only and incompatible with KMP shared code.
- **Instead:** Create a `@CircuitInject @Inject @Composable` presenter function. See `HomePresenter.kt` for the pattern.
- **Enforced by:** `ForbiddenPatternsTest` -- "no class should extend ViewModel or AndroidViewModel", "no file should import ViewModel classes"

### Raw CoroutineScope / launch / async

- **Banned:** `CoroutineScope`, `MainScope`, `GlobalScope`, `launch`, `async` imports in feature/composeApp modules
- **Why:** No structured error handling, no loading state tracking, no timeout management. CenterPost provides all of these.
- **Instead:** Use `rememberCenterPost(dispatchers)` for fire-and-forget, `centerPost.withResult { }` for deferred results, or `CenterPostInteractor` / `CenterPostSubjectInteractor` for business logic.
- **Enforced by:** `ForbiddenPatternsTest` -- "feature modules should not directly use CoroutineScope", "feature modules should not directly use launch or async"

### Hardcoded Dispatchers.*

- **Banned:** `Dispatchers.Default`, `Dispatchers.IO`, `Dispatchers.Main` imports in feature modules
- **Why:** Hardcoded dispatchers are untestable. Tests cannot control thread scheduling.
- **Instead:** Inject `CenterPostDispatchers` interface. In tests, use `TestCenterPostDispatchers()` which routes all dispatchers to `StandardTestDispatcher`.
- **Enforced by:** `ForbiddenPatternsTest` -- "feature modules should not hardcode Dispatchers"

### Android Platform Imports in commonMain

- **Banned:** `android.*`, `androidx.lifecycle.*`, and most `androidx.*` imports in `commonMain` source sets (except `androidx.compose.*`, `androidx.annotation.*`, `androidx.collection.*`)
- **Why:** commonMain must be platform-agnostic. Android-specific imports break iOS compilation.
- **Instead:** Use expect/actual declarations, or KMP-compatible libraries.
- **Enforced by:** `ForbiddenPatternsTest` -- "commonMain source sets should not import android platform packages"

## Testing

### mockk / Mockito

- **Banned:** `io.mockk.*` imports in commonTest
- **Why:** mockk is not thread-safe under Kotest's concurrent spec execution (`SpecExecutionMode.LimitedConcurrency(4)`). Multiple specs running simultaneously with shared mock state causes flaky tests.
- **Instead:** Use fakes with `MutableStateFlow` in dedicated `test/src/commonMain/` modules. Fakes are inherently thread-safe, give precise control, and make tests more readable. Every abstract use case gets a `Fake{Name}`.
- **Enforced by:** `TestDoubleConventionsTest` -- "commonTest should not import mockk"

### runTest

- **Banned:** `kotlinx.coroutines.test.runTest` in test code
- **Why:** Kotest provides native coroutine support. `runTest` is redundant and its virtual time control conflicts with Kotest's execution model.
- **Instead:** Use Kotest `BehaviorSpec` (coroutine-native) + `TestCenterPostDispatchers()` for deterministic dispatching.
- **Enforced by:** `TestFileNamingTest` -- "no runTest in tests"

### UnconfinedTestDispatcher

- **Banned:** `kotlinx.coroutines.test.UnconfinedTestDispatcher` in test code
- **Why:** Not safe under concurrent spec execution (`SpecExecutionMode.LimitedConcurrency`). Unconfined dispatching causes non-deterministic test behavior when specs run in parallel.
- **Instead:** Use `StandardTestDispatcher` via `TestCenterPostDispatchers()`.
- **Enforced by:** `TestFileNamingTest` -- "no UnconfinedTestDispatcher in tests"

## iOS Interop

### sealed interface for Events

- **Banned:** `sealed interface *Event`
- **Why:** Kotlin sealed interfaces export as Obj-C protocols. In Swift, this breaks `Event.Subtype()` constructor syntax -- you cannot instantiate an Obj-C protocol case. Sealed classes export as Obj-C classes with proper subtype constructors.
- **Instead:** Use `sealed class {Feature}Event` for all Circuit event types.
- **Enforced by:** `CircuitConventionsTest` -- "no sealed interface should be named *Event"
