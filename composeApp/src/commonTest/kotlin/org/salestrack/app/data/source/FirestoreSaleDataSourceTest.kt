package org.salestrack.app.data.source

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.domain.model.NewSaleInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FirestoreSaleDataSourceTest {

    @Test
    fun should_add_sale_with_stub_backend() = runTest {
        val timeProvider = FakeTimeProvider(20_000L)
        val dataSource = FirestoreSaleDataSource(
            initialSales = MockSalesFactory.create(timeProvider),
            timeProvider = timeProvider,
        )

        val result = dataSource.addSale(
            NewSaleInput(
                productName = "Producto Stub",
                category = "General",
                quantity = 2,
                unitPrice = 10_000.0,
                discount = 500.0,
                sellerName = "Ana",
            ),
        )

        assertTrue(result is AppResult.Success)
        val sales = dataSource.observeSales().first()
        assertEquals(5, sales.size)
        assertEquals("Producto Stub", sales.last().productName)
    }

    @Test
    fun should_update_existing_sale() = runTest {
        val timeProvider = FakeTimeProvider(30_000L)
        val dataSource = FirestoreSaleDataSource(
            initialSales = MockSalesFactory.create(timeProvider),
            timeProvider = timeProvider,
        )
        val original = dataSource.observeSales().first().first()

        val result = dataSource.updateSale(
            original.copy(quantity = original.quantity + 2),
        )

        assertTrue(result is AppResult.Success)
        val updated = dataSource.observeSales().first().first { it.id == original.id }
        assertEquals(original.quantity + 2, updated.quantity)
    }

    @Test
    fun should_soft_delete_sale() = runTest {
        val timeProvider = FakeTimeProvider(40_000L)
        val dataSource = FirestoreSaleDataSource(
            initialSales = MockSalesFactory.create(timeProvider),
            timeProvider = timeProvider,
        )
        val saleId = dataSource.observeSales().first().first().id

        val result = dataSource.softDeleteSale(saleId)

        assertTrue(result is AppResult.Success)
        val deleted = dataSource.observeSales().first().first { it.id == saleId }
        assertTrue(deleted.isDeleted)
    }
}
