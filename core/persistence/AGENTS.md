# core:persistence

## Purpose

Shared infrastructure for on-device storage, split into `api`, `impl`, and `test` submodules. The umbrella owns the per-platform [SqlDriver][app.cash.sqldelight.db.SqlDriver] construction — `DatabaseDriverFactory` and its Android/iOS actuals — while the single application-wide SQLDelight `AppDatabase` is aggregated in `composeApp`. Each feature contributes its own `.sq` schemas inside `features/{name}/impl/data/sqldelight/`.

## Pattern: single shared DB, feature-owned schemas

This is the [Slack][slack-modularize] / [Cash App][cashapp-sqldelight] SQLDelight pattern: one on-disk database, each feature owns its own `.sq` files (queries + schema), and a leaf module aggregates them into one generated `AppDatabase` class via SQLDelight's `dependency(project(...))` declaration.

The key architectural property is that the aggregator (`composeApp`) does not reference feature *content* — it only references feature *project paths*. composeApp's `sqldelight { databases { create("AppDatabase") { dependency(...) } } }` block lists which feature data modules contribute schemas; the actual entity definitions, queries, and migrations live entirely in each feature's `impl/data/sqldelight/` tree.

This preserves the codebase-wide "core never imports features" Konsist rule because composeApp is the leaf consumer that already depends on every feature module — there is no upward-pointing dependency edge created by the aggregation.

## Why SQLDelight (not Room)

| Concern | Room | SQLDelight |
|---------|------|------------|
| Schema location | Central `@Database(entities = […])` lists every entity by class | Feature-local `.sq` files; aggregator only lists project paths |
| Schema change blast radius | composeApp recompiles when any feature adds a column (KSP graph invalidation) | Per-module compilation; feature changes contained |
| Migration ownership | Central `Migration` array on the builder — every feature edits one place | `.sqm` files colocated with `.sq` — each feature owns its own version sequence |
| Public exemplar at scale | NIA (sample) | Slack, Cash App, Square |

Room's central annotation is a coordination chokepoint at 150-dev scale. SQLDelight's project-path aggregation is content-blind, so feature owners ship schema changes without touching shared code.

## Architecture

```
core/persistence/api          -> DatabaseDriverFactory contract
  └ commonMain                  DatabaseDriverFactory (interface)
core/persistence/impl         -> Per-platform driver actuals
  └ androidMain                 AndroidDatabaseDriverFactory → AndroidSqliteDriver
  └ iosMain                     IosDatabaseDriverFactory → NativeSqliteDriver
core/persistence/test         -> Test fixtures
  └ commonMain                  FakeDatabaseDriverFactory (records DB names; in-memory driver)
  └ androidMain                 createInMemoryDriver actual → JdbcSqliteDriver(IN_MEMORY)
  └ iosMain                     createInMemoryDriver actual → NativeSqliteDriver(":memory:")

composeApp                    -> SQLDelight aggregator (owns AppDatabase)
  └ build.gradle.kts            sqldelight { databases.create("AppDatabase") { dependency(...) } }
                                Adds dependency(project(":features:<name>:impl:data")) per
                                contributing feature.

features/{name}/impl/data    -> Feature schema contributors (when persistence is needed)
  └ build.gradle.kts            Applies SQLDelight plugin, declares same AppDatabase name
  └ src/commonMain/sqldelight/  Feature-owned .sq files (tables, queries) + .sqm migrations
```

## Public API

| Type | Module | Description |
|------|--------|-------------|
| `DatabaseDriverFactory` | api | Per-platform `SqlDriver` factory. `create(schema, name)` returns a configured driver for the given `AppDatabase.Schema` and on-disk database name. |
| `AndroidDatabaseDriverFactory` | impl/androidMain | `@ContributesBinding(AppScope::class)` actual — wraps `AndroidSqliteDriver` with the `Application` context. |
| `IosDatabaseDriverFactory` | impl/iosMain | `@ContributesBinding(AppScope::class)` actual — wraps `NativeSqliteDriver`. |
| `FakeDatabaseDriverFactory` | test | Records requested database names; returns a fresh in-memory driver per call (`JdbcSqliteDriver(IN_MEMORY)` on JVM, `NativeSqliteDriver(":memory:")` on iOS). |

## Usage

`composeApp` consumes the factory and the generated `AppDatabase` together (typical wiring lives in `composeApp/{androidMain,iosMain}/AppGraph.kt` next to the rest of the graph):

```kotlin
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class AppDatabaseProvider(driverFactory: DatabaseDriverFactory) {
    val database: AppDatabase = AppDatabase(
        driver = driverFactory.create(AppDatabase.Schema, "sampleplatter.db"),
    )
}
```

A feature that needs persistence:

1. Apply the SQLDelight plugin in `features/{name}/impl/data/build.gradle.kts` and declare the same `AppDatabase` name + `packageName`:
   ```kotlin
   plugins {
       id("sampleplatter.kmp.data")
       alias(libs.plugins.sqldelight)
   }
   sqldelight {
       databases {
           create("AppDatabase") {
               packageName.set("com.jkjamies.sampleplatter.persistence")
           }
       }
   }
   ```
2. Add `.sq` files under `src/commonMain/sqldelight/com/jkjamies/sampleplatter/features/{name}/` — these contain the feature's tables and queries.
3. Add `.sqm` migration files colocated with `.sq` for schema changes — each `.sq` file has its own version sequence.
4. In `composeApp/build.gradle.kts`, add `dependency(project(":features:<name>:impl:data"))` inside the `create("AppDatabase")` block so the aggregator picks up the feature's schema.
5. Inject the generated `<feature>Queries` class (exposed off `AppDatabase`) into the feature's data layer; expose results through the feature's repository interface.

## Driver

- **Android**: `AndroidSqliteDriver` (system SQLite, integrates with the platform's lifecycle).
- **iOS**: `NativeSqliteDriver` (uses iOS's bundled SQLite via SQLiter).
- **Tests (JVM)**: `JdbcSqliteDriver(IN_MEMORY)` — pure JVM, fast, no on-disk file.
- **Tests (iOS)**: `NativeSqliteDriver(":memory:")` — in-memory variant for native test runs.

No expect/actual driver split is exposed to features — `DatabaseDriverFactory` hides the platform difference behind a single interface.

## Rules

- Core modules never import from features.
- Only `core:persistence` (api/impl/test), `composeApp`, and feature `impl/data` modules may import `app.cash.sqldelight.*`. Konsist (`PersistenceConventionsTest`) enforces this.
- Each feature owns its own `.sq` files under `features/{name}/impl/data/src/commonMain/sqldelight/`. Hard-coded paths and direct `AndroidSqliteDriver` / `NativeSqliteDriver` construction in feature code are forbidden — the factory is the only entry point.
- `composeApp` is the only module that declares `dependency(project(...))` inside its `sqldelight.databases.create("AppDatabase")` block. Features apply the plugin and declare the same database name + `packageName`, but never aggregate other features.
- Test code uses `FakeDatabaseDriverFactory` (or a real in-memory driver via `createInMemoryDriver` directly when no factory wrapping is needed).
- All Metro bindings (`AndroidDatabaseDriverFactory`, `IosDatabaseDriverFactory`, and feature `AppDatabaseProvider`-style classes) are bound via `@ContributesBinding(AppScope::class)`; no `@Inject` annotation is added — Metro infers the constructor from `@ContributesBinding`.

[slack-modularize]: https://slack.engineering/stabilize-modularize-modernize-how-slack-improved-developer-experience-on-android/
[cashapp-sqldelight]: https://github.com/cashapp/sqldelight
