package com.salestrack.di

import com.salestrack.db.SalesTrackDatabase
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SalesTrackDatabase.Schema.create(driver)
        SalesTrackDatabase(driver)
    }
}
