package com.salestrack.di

import com.salestrack.db.SalesTrackDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single {
        val driver = AndroidSqliteDriver(SalesTrackDatabase.Schema, get(), "salestrack.db")
        SalesTrackDatabase(driver)
    }
}
