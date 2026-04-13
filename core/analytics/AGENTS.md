# core:analytics

## Purpose

Event tracking abstraction that features use without knowing the analytics provider. Shell module — contains contracts and a logging implementation that prints to console. Replace with a real provider (Firebase Analytics, Mixpanel, etc.) when ready.

## Architecture

```
core/analytics/api   -> AnalyticsEvent, AnalyticsDispatcher, TrackAnalyticsEvent interactor (feature-visible)
core/analytics/impl  -> LoggingAnalyticsDispatcher, TrackAnalyticsEventImpl (DI-only, never imported directly)
core/analytics/test  -> FakeAnalyticsDispatcher, FakeTrackAnalyticsEvent for consumer tests
```

## Public API

| Type | Module | Description |
|------|--------|-------------|
| `AnalyticsEvent` | api | Interface: `name: String`, `properties: Map<String, Any>` |
| `AnalyticsDispatcher` | api | Interface: `track(event)`, `trackScreenView(screenName)` |
| `TrackAnalyticsEvent` | api | Abstract `CenterPostInteractor<AnalyticsEvent, Unit>` for presenter consumption |
| `FakeAnalyticsDispatcher` | test | Records events and screen views for test assertions |
| `FakeTrackAnalyticsEvent` | test | Records tracked events for test assertions |

## Consumption Pattern

Same dual-access pattern as `core:feature-flag`:

**Presenters** use the `TrackAnalyticsEvent` CenterPost interactor:

```kotlin
@CircuitInject(MyScreen::class, AppScope::class)
@Composable
fun MyPresenter(
    trackAnalyticsEvent: TrackAnalyticsEvent,
    dispatchers: CenterPostDispatchers,
): MyUiState {
    val centerPost = rememberCenterPost(dispatchers)
    return MyUiState(
        eventSink = { event ->
            when (event) {
                is MyEvent.ButtonClicked ->
                    centerPost { trackAnalyticsEvent(MyAnalyticsEvent.ButtonTapped) }
            }
        },
    )
}
```

The `inProgress` loading state exists but is simply not collected — it's opt-in with zero overhead. The value of wrapping analytics in CenterPost: structured execution, error handling, timeout protection, dispatcher correctness.

**Domain/data layers** inject `AnalyticsDispatcher` directly:

```kotlin
@ContributesBinding(AppScope::class)
class MyRepositoryImpl(
    private val analyticsDispatcher: AnalyticsDispatcher,
) : MyRepository {
    override fun getData(): Flow<Data> {
        analyticsDispatcher.track(MyAnalyticsEvent.DataFetched)
        // ...
    }
}
```

**Screen views** are tracked automatically via `AnalyticsNavigationListener` in `composeApp` on `goTo` and `resetRoot` navigation — no manual tracking needed.

**Pop is not tracked.** Back navigation does not fire a screen view for the revealed screen. Tracking pop correctly requires the full backstack (to know what was revealed without firing for intermediate screens on multi-pop). This is deferred to the `circuitx-navigation` migration — Circuit's `NavigationEventListener.pop(backStack, result)` provides the backstack.

**Deep links** track only the final destination screen, not intermediate screens in the chain. Both platforms bypass `InterceptingNavigator` for deep link navigation and explicitly call `analyticsListener.onGoTo(intercepted.last())`.

**Feature events** are defined in `features/{name}/api/domain/`:

```kotlin
sealed class HomeAnalyticsEvent(
    override val name: String,
    override val properties: Map<String, Any> = emptyMap(),
) : AnalyticsEvent {
    data object HeroBannerTapped : HomeAnalyticsEvent("hero_banner_tapped")
}
```

## Rules

- Core modules never import from features
- Features MUST depend on `core:analytics:api` only, never `core:analytics:impl`
- `impl` is wired exclusively through Metro `@ContributesBinding` in `AppScope`
- **Presenters** must use `TrackAnalyticsEvent` interactor, never `AnalyticsDispatcher` directly (Konsist-enforced)
- **Domain/data layers** must use `AnalyticsDispatcher`, never `TrackAnalyticsEvent` (Konsist-enforced)
- Test code should use fakes from `core:analytics:test`
- Remote SDK is swappable: replace `LoggingAnalyticsDispatcher` binding in impl without touching api or consumers
