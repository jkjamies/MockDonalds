# core:logger

## Purpose

Structured application logging via Touchlab's [Kermit](https://kermit.touchlab.co/), split into `api`, `impl`, and `test` submodules. Features depend only on `api`; runtime configuration (severity threshold, writer set) is wired in `impl`; test graphs (`navint-tests`) depend on `test` to bind a no-op `LoggerInitializer` without pulling in `:impl`. Additional sinks (crash reporters, remote monitoring) bridge in via a multi-bound `Set<LogWriter>` so the logger surface never leaks SDK-specific symbols.

## Architecture

```
core/logger/api   -> Logger, Severity, LogWriter typealiases over Kermit (feature-visible)
                     featureLogger(tag) helper
                     LoggerInitializer interface
core/logger/impl  -> KermitLoggerInitializer (configures Kermit at app startup)
                     LoggerProviders (@Multibinds Set<LogWriter> seam for additional writers)
core/logger/test  -> NoOpLoggerInitializer + LoggerTestBindings (@ContributesTo @Provides)
                     so test graphs (navint-tests, future test app graphs) bind LoggerInitializer
                     without pulling in :impl
```

## Public API

| Type | Module | Description |
|------|--------|-------------|
| `Logger` | api | Typealias for `co.touchlab.kermit.Logger`. Top-level severity methods (`v`, `d`, `i`, `w`, `e`, `a`) and tag-scoped instances via `Logger.withTag(...)` |
| `Severity` | api | Typealias for `co.touchlab.kermit.Severity`. Verbose, Debug, Info, Warn, Error, Assert |
| `LogWriter` | api | Typealias for `co.touchlab.kermit.LogWriter`. Implement to bridge logs into another sink (e.g. crash reporter) |
| `featureLogger(tag)` | api | Helper returning `Logger.withTag(tag)` for per-feature tagged logging |
| `LoggerInitializer` | api | Interface with `initialize()`. Exposed on `AppGraph` so `composeApp` can invoke it after graph creation without depending on `core:logger:impl` |
| `KermitLoggerInitializer` | impl | `@SingleIn(AppScope)` `@ContributesBinding`. Sets `minSeverity` from `AppBuildConfig.isDebug` and installs Kermit's `platformLogWriter()` (Android = `LogcatWriter`; iOS = `XcodeSeverityWriter`) plus all `Set<LogWriter>` contributed by other modules |
| `LoggerProviders` | impl | `@ContributesTo(AppScope)`. Declares `Set<LogWriter>` as `@Multibinds(allowEmpty = true)` so other modules can `@ContributesIntoSet` writers |
| `NoOpLoggerInitializer` | test | `LoggerInitializer` whose `initialize()` is a no-op — Kermit stays untouched in test graphs |
| `LoggerTestBindings` | test | `@ContributesTo(AppScope)` provider binding `LoggerInitializer` to `NoOpLoggerInitializer`. Test graphs depend on `core:logger:test` instead of `:impl` |

## Usage

Features create a tagged logger and call severity methods directly:

```kotlin
private val log = featureLogger("Home")

class HomePresenter @Inject constructor(...) : Presenter<HomeState> {
    override fun present(): HomeState {
        log.i { "Composing home" }
        log.e(throwable) { "Failed to load menu" }
    }
}
```

The top-level `Logger.i { ... }` API also works for module-level logs without a tag.

## Initialization

`composeApp` invokes `graph.loggerInitializer.initialize()` immediately after Metro graph creation on both platforms (Android `App.kt`, iOS `IosApp.kt`). After that, all `Logger.*` calls route through the configured writer set.


## Adding a writer

A module wanting to bridge logs into its own sink (crash reporter, remote monitoring, etc.):

```kotlin
@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
class MonitoringLogWriter @Inject constructor(
    /* SDK handle */
) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        // bridge to the sink
    }
}
```

Metro auto-collects every `@ContributesIntoSet` contributor into the `Set<LogWriter>` consumed by `LoggerInitializer`. The logger module never references the sink's SDK directly.

## Rules

- Features MUST depend on `core:logger:api` only, never `core:logger:impl`
- Raw `co.touchlab.kermit.*` imports outside `core:logger` are rejected by Konsist — use the `Logger` / `Severity` / `LogWriter` typealiases from `core:logger:api`
- `core:logger:api` is auto-wired into `sampleplatter.kmp.domain` / `kmp.data` / `kmp.presentation` plugins, so feature impl modules can `import com.jkjamies.sampleplatter.core.logger.Logger` without declaring the dependency
- Test code asserts on log output via `kermit-test`'s `TestLogWriter`, re-exposed by `core:test-fixtures`
