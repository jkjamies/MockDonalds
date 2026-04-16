---
name: add-analytics-events
description: "Add analytics event tracking to a feature — event definitions, presenter wiring, and domain/data dispatch. Use when adding user behavior tracking. NOTE: core:analytics exists with AnalyticsEvent interface, TrackAnalyticsEvent interactor, and LoggingAnalyticsDispatcher — production SDK integration is not yet finalized."
---

# Add Analytics Events

> **Infrastructure status**: `core:analytics` provides `AnalyticsEvent` interface, `TrackAnalyticsEvent` CenterPost interactor (for presenters), `AnalyticsDispatcher` (for domain/data), and automatic screen view tracking via `AnalyticsNavigationListener`. Current implementation logs to console via `LoggingAnalyticsDispatcher`. Production SDK integration (e.g., Firebase, Amplitude) is planned but not yet wired.

Add analytics event tracking to a feature.

**Parameters**: feature name, events to track (optional if spec provides them)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — feature name + event descriptions, e.g., `/add-analytics-events deals item tapped, filter changed`.
2. **`@file` reference** — e.g., `/add-analytics-events @specs/deals-analytics.md`. Extract event names, triggers, and properties from the spec. Template: `.agents/templates/new-spec.md` (Analytics section) or `.agents/templates/change-spec.md` (Analytics Changes section).
3. **Inline description** — free text listing events and their triggers.

## Reference

- Core analytics module: `core/analytics/AGENTS.md`
- `AnalyticsEvent` interface: `core/analytics/api/src/commonMain/.../AnalyticsEvent.kt`
- `TrackAnalyticsEvent` interactor: `core/analytics/api/src/commonMain/.../TrackAnalyticsEvent.kt`
- `AnalyticsDispatcher` interface: `core/analytics/api/src/commonMain/.../AnalyticsDispatcher.kt`

## Files to Create / Modify

### 1. Event Definitions — `api/domain/`

`features/{feature}/api/domain/src/commonMain/kotlin/com/mockdonalds/app/features/{feature}/api/domain/{Feature}AnalyticsEvent.kt`

```kotlin
package com.mockdonalds.app.features.{feature}.api.domain

import com.mockdonalds.app.core.analytics.AnalyticsEvent

sealed class {Feature}AnalyticsEvent(
    override val name: String,
    override val properties: Map<String, Any> = emptyMap(),
) : AnalyticsEvent {

    data object ScreenViewed : {Feature}AnalyticsEvent(
        name = "{feature}_screen_viewed",
    )

    data class ItemTapped(val itemId: String, val position: Int) : {Feature}AnalyticsEvent(
        name = "{feature}_item_tapped",
        properties = mapOf("item_id" to itemId, "position" to position),
    )

    data object RefreshTriggered : {Feature}AnalyticsEvent(
        name = "{feature}_refresh_triggered",
    )
}
```

### 2. Presenter Wiring — `impl/presentation/`

Presenters use `TrackAnalyticsEvent` CenterPost interactor (**never** `AnalyticsDispatcher` directly — Konsist-enforced):

```kotlin
@CircuitInject({Feature}Screen::class, AppScope::class)
@Inject
@Composable
fun {Feature}Presenter(
    trackAnalyticsEvent: TrackAnalyticsEvent,  // ← inject
    dispatchers: CenterPostDispatchers,
    // ... other dependencies
): {Feature}UiState {
    val centerPost = rememberCenterPost(dispatchers)

    return {Feature}UiState(
        eventSink = { event ->
            when (event) {
                is {Feature}Event.OnItemTapped -> {
                    centerPost { trackAnalyticsEvent({Feature}AnalyticsEvent.ItemTapped(event.id, event.position)) }
                    // ... other handling
                }
            }
        },
    )
}
```

### 3. Domain/Data Wiring (if needed) — `impl/domain/` or `impl/data/`

Domain and data layers inject `AnalyticsDispatcher` directly (**never** `TrackAnalyticsEvent` — Konsist-enforced):

```kotlin
class {Name}Impl(
    private val analyticsDispatcher: AnalyticsDispatcher,  // ← inject
) : {Name}() {
    override suspend fun doWork(params: Params): Result {
        analyticsDispatcher.track({Feature}AnalyticsEvent.SomeEvent)
        // ... business logic
    }
}
```

### 4. Screen View Tracking

Screen views are tracked automatically by `AnalyticsNavigationListener` — no manual code needed. The listener fires `screenView` events when Circuit navigates to a new screen.

**Exception**: If you need custom screen view properties or conditional tracking, override in the presenter.

## Event Naming Conventions

| Pattern | Example | Use |
|---------|---------|-----|
| `{feature}_screen_viewed` | `deals_screen_viewed` | Screen impression (usually automatic) |
| `{feature}_{noun}_{verb_past}` | `deals_item_tapped` | User interaction |
| `{feature}_{action}_{result}` | `deals_submit_succeeded` | Outcome tracking |
| `{feature}_{noun}_{adjective}` | `deals_list_empty` | State observation |

## Property Conventions

- Use `snake_case` for property keys
- Keep property values as primitives (`String`, `Int`, `Boolean`, `Double`)
- Include positional context where relevant (`position`, `index`)
- Include identifiers for drilldown (`item_id`, `category_id`)
- Never include PII (names, emails, device IDs)

## Key Rules

- **Events are sealed classes** — not sealed interfaces (iOS interop)
- **Events live in `api/domain/`** — they're part of the feature's public contract
- **Presenters use `TrackAnalyticsEvent`** — CenterPost interactor, async dispatch
- **Domain/data use `AnalyticsDispatcher`** — direct injection, synchronous dispatch
- **Screen views are automatic** — don't manually track unless custom properties needed
- **Deep links track final destination only** — not intermediate navigation screens

## Build File Dependencies

`api/domain/build.gradle.kts` needs:
```kotlin
commonMain.dependencies {
    api(project(":core:analytics:api"))
}
```

`impl/presentation/build.gradle.kts` — already has `core:analytics:api` transitively via convention plugin (verify).

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify-smart` skill to validate all changes.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
