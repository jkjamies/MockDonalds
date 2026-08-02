package com.jkjamies.sampleplatter.core.persistence

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/**
 * Per-platform [SqlDriver] factory for the single application-wide SQLDelight
 * database.
 *
 * Mirrors `core:network`'s `HttpClientFactory` shape: the umbrella owns the
 * platform-specific driver construction, while the consumer (`composeApp`'s
 * `AppDatabase` wiring) supplies the schema and database name. Features
 * contribute `.sq` files that compile into the single shared `AppDatabase`,
 * matching the Slack / Cash App SQLDelight pattern.
 *
 * Typical usage (from `composeApp`'s graph wiring):
 *
 * ```
 * @ContributesBinding(AppScope::class)
 * @SingleIn(AppScope::class)
 * class AppDatabaseProvider(driverFactory: DatabaseDriverFactory) {
 *     val database: AppDatabase = AppDatabase(
 *         driver = driverFactory.create(AppDatabase.Schema, "sampleplatter.db"),
 *     )
 * }
 * ```
 */
interface DatabaseDriverFactory {
    fun create(schema: SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver
}
