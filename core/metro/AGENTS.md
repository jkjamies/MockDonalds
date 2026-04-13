# Core: Metro

## Purpose

Shared Metro DI graph contract. Defines the `AppGraph` interface that both production (`composeApp`) and test (`navint-tests`) modules implement with `@DependencyGraph`.

## Key Types

| Type | Role |
|------|------|
| `AppGraph` | Interface declaring DI graph outputs (`circuit`, `analyticsDispatcher`, `authManager`, `appBuildConfig`). Consumer modules extend this with `@DependencyGraph(AppScope::class)` |

## Dependencies

- `core:circuit` — provides `Circuit` type and `CircuitProviders` (factory aggregation)
- `core:analytics:api` — provides `AnalyticsDispatcher` interface
- `core:auth:api` — provides `AuthManager` interface
- `core:build-config` — provides `AppBuildConfig` (market/env configuration)

## Design

`AppGraph` is intentionally annotation-free. Each consuming module applies `@DependencyGraph`:
- `composeApp` — real bindings (impl/domain, impl/data)
- `navint-tests` — fake bindings (test/ modules with `@ContributesBinding`)

Metro generates different implementations based on each module's classpath. Same interface, different bindings.

## Rules

- Must NOT import from any feature module
- Must NOT import from `impl/` modules
- Must NOT have `@DependencyGraph` annotation (consumers add it)
