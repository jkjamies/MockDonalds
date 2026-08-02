package com.jkjamies.sampleplatter.core.persistence.test

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver

internal actual fun createInMemoryDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver =
    NativeSqliteDriver(schema = schema, name = ":memory:")
