package com.mockdonalds.app.core.persistence.test

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

internal actual fun createInMemoryDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    schema.create(driver)
    return driver
}
