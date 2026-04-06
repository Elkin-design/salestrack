package org.salestrack.app.data.source

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.domain.model.NewProductInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FirestoreInventoryDataSourceTest {

    @Test
    fun should_add_product_with_stub_backend() = runTest {
        val timeProvider = FakeTimeProvider(20_000L)
        val initialProducts = MockInventoryFactory.create()
        val dataSource = FirestoreInventoryDataSource(
            initialProducts = initialProducts,
            initialMovements = MockInventoryFactory.createInitialMovements(timeProvider, initialProducts),
            timeProvider = timeProvider,
        )

        val result = dataSource.addProduct(
            NewProductInput(
                name = "Miel",
                description = "Frasco",
                unitPrice = 12_000.0,
                unit = "Unidad",
                barcode = "771111",
                category = "Despensa",
                initialStock = 9,
                minimumStock = 3,
            ),
        )

        assertTrue(result is AppResult.Success)
        val products = dataSource.observeProducts().first()
        assertEquals(4, products.size)
        assertEquals("Miel", products.last().name)
    }

    @Test
    fun should_export_catalog_as_csv() = runTest {
        val timeProvider = FakeTimeProvider(30_000L)
        val initialProducts = MockInventoryFactory.create()
        val dataSource = FirestoreInventoryDataSource(
            initialProducts = initialProducts,
            initialMovements = MockInventoryFactory.createInitialMovements(timeProvider, initialProducts),
            timeProvider = timeProvider,
        )

        val result = dataSource.exportCatalogCsv()

        assertTrue(result is AppResult.Success)
        assertTrue(result.value.content.contains("name,description,unitPrice"))
    }

    @Test
    fun should_return_low_stock_products() = runTest {
        val timeProvider = FakeTimeProvider(40_000L)
        val initialProducts = MockInventoryFactory.create()
        val dataSource = FirestoreInventoryDataSource(
            initialProducts = initialProducts,
            initialMovements = MockInventoryFactory.createInitialMovements(timeProvider, initialProducts),
            timeProvider = timeProvider,
        )

        val result = dataSource.getLowStockProducts()

        assertTrue(result is AppResult.Success)
        assertEquals(1, result.value.size)
    }
}
