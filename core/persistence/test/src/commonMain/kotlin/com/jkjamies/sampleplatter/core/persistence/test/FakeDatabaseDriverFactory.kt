package com.jkjamies.sampleplatter.core.persistence.test

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.jkjamies.sampleplatter.core.persistence.DatabaseDriverFactory

/**
 * In-memory [DatabaseDriverFactory] for tests. Records the database names it
 * was asked to create so feature data tests can assert wiring without needing
 * an on-disk file. Each call returns a fresh in-memory driver — tests do not
 * share state across [create] invocations.
 */
class FakeDatabaseDriverFactory : DatabaseDriverFactory {
    val createdDatabases: MutableList<String> = mutableListOf()

    override fun create(schema: SqlSchema<QueryResult.Value<Unit>>, name: String): SqlDriver {
        createdDatabases += name
        return createInMemoryDriver(schema)
    }
}

internal expect fun createInMemoryDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver
