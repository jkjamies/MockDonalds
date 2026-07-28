package com.jkjamies.sampleplatter.persistence

import com.jkjamies.sampleplatter.core.persistence.DatabaseDriverFactory
import com.jkjamies.sampleplatter.features.order.data.MenuItemQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface DatabaseProviders {

    @Provides
    @SingleIn(AppScope::class)
    fun provideAppDatabase(driverFactory: DatabaseDriverFactory): AppDatabase = AppDatabase(
        driver = driverFactory.create(AppDatabase.Schema, DATABASE_NAME),
    )

    @Provides
    @SingleIn(AppScope::class)
    fun provideMenuItemQueries(database: AppDatabase): MenuItemQueries = database.menuItemQueries

    private companion object {
        const val DATABASE_NAME = "sampleplatter.db"
    }
}
