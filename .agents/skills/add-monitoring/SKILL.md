---
name: add-monitoring
description: "Add observability instrumentation to a feature — logging, performance markers, health checks, and crash context. Use when adding production monitoring to a feature. NOTE: core:monitoring module is planned but not yet implemented — this skill documents the target patterns and scaffolds what's possible today."
---

# Add Monitoring

> **Infrastructure status**: `core:monitoring` is planned but not yet implemented. The target architecture includes New Relic Mobile SDK (or equivalent) for crash reporting, network performance monitoring, and distributed tracing. Today, logging goes through `println` in non-prod builds via the network layer's `Logging` plugin. This skill documents the target patterns so code is monitoring-ready when the infrastructure lands.

Add observability instrumentation to a feature.

**Parameters**: feature name, monitoring scope (optional if spec provides them)

## Context (optional)

The user may provide additional context in three ways — all are optional:

1. **Bare** — feature name + what to monitor, e.g., `/add-monitoring order track checkout flow timing`.
2. **`@file` reference** — e.g., `/add-monitoring @specs/order-monitoring.md`. Extract metrics, traces, health checks, and alerting thresholds from the spec.
3. **Inline description** — free text describing monitoring needs.

## What Can Be Done Today

### 1. Structured Logging Points

Add logging that will be routable to a monitoring backend when `core:monitoring` lands:

```kotlin
// In impl/data or impl/domain — log key operations
// These will be replaced with monitoring SDK calls when available
private fun logOperation(operation: String, metadata: Map<String, Any> = emptyMap()) {
    // TODO: Replace with core:monitoring when available
    // For now, console logging in non-prod only
}
```

### 2. Performance-Aware Patterns

Structure code so performance instrumentation is easy to add:

- **Use CenterPost `inProgress` flows** — already provide loading state tracking
- **Keep data transformations in measurable units** — separate mapping from fetching
- **Use `kotlin.time.measureTimedValue`** where timing matters for future metrics

### 3. Error Context

Enrich error handling with context that monitoring will consume:

```kotlin
// In repository or data source implementations
try {
    client.get("{endpoint}").body<{Dto}>()
} catch (e: Exception) {
    // TODO: Report to core:monitoring when available
    // Include: feature name, operation, endpoint, duration, error type
    throw e
}
```

### 4. Health Check Readiness

Define what "healthy" means for the feature:

| Check | Description | Threshold |
|-------|-------------|-----------|
| API reachability | Can reach `{service}BaseUrl` | < 5s response |
| Data freshness | Last successful fetch | < 5 min ago |
| Error rate | Failed requests / total | < 5% |

## Target Architecture (when core:monitoring lands)

### Monitoring Provider Interface

```kotlin
// Planned for core:monitoring:api
interface MonitoringProvider {
    fun startTrace(name: String): Trace
    fun recordMetric(name: String, value: Double, unit: MetricUnit)
    fun recordError(error: Throwable, context: Map<String, Any>)
    fun addBreadcrumb(message: String, metadata: Map<String, Any>)
}

interface Trace : AutoCloseable {
    fun addAttribute(key: String, value: String)
    fun setStatus(status: TraceStatus)
}
```

### Feature Integration Pattern

```kotlin
// In impl/data — wrap network calls with traces
class {Feature}RepositoryImpl(
    private val remoteDataSource: {Feature}RemoteDataSource,
    private val monitoring: MonitoringProvider,  // ← future injection
) : {Feature}Repository {
    override fun get{Feature}(): Flow<{DomainModel}> =
        remoteDataSource.get{Feature}()
            .onStart { monitoring.startTrace("{feature}_fetch") }
            .onCompletion { monitoring.recordMetric("{feature}_fetch_complete", 1.0, MetricUnit.COUNT) }
            .catch { e ->
                monitoring.recordError(e, mapOf("feature" to "{feature}", "operation" to "fetch"))
                throw e
            }
            .map { it.to{DomainModel}() }
}
```

### Presenter Integration Pattern

```kotlin
// In presenters — track user flow timing
val centerPost = rememberCenterPost(dispatchers)
// Future: centerPost operations automatically traced via CenterPost + monitoring integration
```

## Monitoring Naming Conventions

| Pattern | Example | Use |
|---------|---------|-----|
| `{feature}_{operation}` | `order_fetch` | Trace name |
| `{feature}_{metric}_{unit}` | `order_load_duration_ms` | Metric name |
| `{feature}_{noun}_{state}` | `order_cache_hit` | Counter name |

## Key Rules

- **Don't block on core:monitoring** — add structure now, SDK calls later
- **Monitoring is NOT analytics** — analytics tracks user behavior, monitoring tracks system health
- **Keep monitoring out of api/ modules** — it's an implementation concern
- **Presenters don't monitor directly** — CenterPost will integrate with monitoring at the framework level
- **Don't log PII** — same rules as analytics

## Post-Change Verification — MANDATORY

**Work is NEVER complete until verification passes.** Run the `verify` skill to validate all changes.

If ANY check fails, fix the issue and re-run. Do not declare the task complete until verification passes.
