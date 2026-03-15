package com.salestrack.di

import com.salestrack.db.SalesTrackDatabase
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.salestrack.domain.util.ReportGenerator
import com.salestrack.util.DesktopReportGenerator
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.ColumnAdapter
import com.salestrack.db.ProductEntity
import com.salestrack.db.SaleEntity
import org.koin.core.module.Module
import org.koin.dsl.module

val booleanAdapter = object : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean = databaseValue != 0L
    override fun encode(value: Boolean): Long = if (value) 1L else 0L
}

actual fun platformModule(): Module = module {
    single {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SalesTrackDatabase.Schema.create(driver)
        SalesTrackDatabase(
            driver = driver,
            ProductEntityAdapter = ProductEntity.Adapter(
                stockAdapter = IntColumnAdapter,
                minStockThresholdAdapter = IntColumnAdapter
            ),
            SaleEntityAdapter = SaleEntity.Adapter(
                quantityAdapter = IntColumnAdapter
            )
        )
    }
    single<ReportGenerator> { DesktopReportGenerator() }
}
