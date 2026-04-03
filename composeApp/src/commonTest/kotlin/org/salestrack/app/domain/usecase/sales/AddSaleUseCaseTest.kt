package org.salestrack.app.domain.usecase.sales

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.data.repository.FakeInventoryRepository
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.NewSaleInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddSaleUseCaseTest {

    @Test
    fun should_add_sale_when_input_is_valid() = runTest {
        val repository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(emptyList()),
            timeProvider = FakeTimeProvider(1000L),
        )
        val useCase = AddSaleUseCase(repository)

        val result = useCase(
            NewSaleInput(
                productName = "Producto A",
                category = "General",
                quantity = 2,
                unitPrice = 10.0,
                discount = 1.0,
                sellerName = "Ana",
            ),
        )

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun should_fail_when_quantity_is_invalid() = runTest {
        val repository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(emptyList()),
            timeProvider = FakeTimeProvider(1000L),
        )
        val useCase = AddSaleUseCase(repository)

        val result = useCase(
            NewSaleInput(
                productName = "Producto A",
                category = "General",
                quantity = 0,
                unitPrice = 10.0,
                discount = 0.0,
                sellerName = "Ana",
            ),
        )

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_fail_when_price_is_invalid() = runTest {
        val repository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(emptyList()),
            timeProvider = FakeTimeProvider(1000L),
        )
        val useCase = AddSaleUseCase(repository)

        val result = useCase(
            NewSaleInput(
                productName = "Producto A",
                category = "General",
                quantity = 1,
                unitPrice = 0.0,
                discount = 0.0,
                sellerName = "Ana",
            ),
        )

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_deduct_inventory_stock_when_product_exists_in_catalog() = runTest {
        val timeProvider = FakeTimeProvider(1000L)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(emptyList()),
            timeProvider = timeProvider,
        )
        val inventoryRepository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = timeProvider,
        )
        val useCase = AddSaleUseCase(saleRepository, inventoryRepository)

        val result = useCase(
            NewSaleInput(
                productName = "Cafe Premium",
                category = "Bebidas",
                quantity = 2,
                unitPrice = 18_000.0,
                discount = 0.0,
                sellerName = "Ana",
            ),
        )

        assertTrue(result is AppResult.Success)
        val products = inventoryRepository.observeProducts().first()
        assertEquals(10, products.first { it.id == "P-1" }.stock)
    }

    @Test
    fun should_fail_sale_when_inventory_stock_is_insufficient() = runTest {
        val timeProvider = FakeTimeProvider(1000L)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(emptyList()),
            timeProvider = timeProvider,
        )
        val inventoryRepository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = timeProvider,
        )
        val useCase = AddSaleUseCase(saleRepository, inventoryRepository)

        val result = useCase(
            NewSaleInput(
                productName = "Galletas Integrales",
                category = "Snacks",
                quantity = 10,
                unitPrice = 8_500.0,
                discount = 0.0,
                sellerName = "Ana",
            ),
        )

        assertTrue(result is AppResult.Failure)
        val sales = saleRepository.observeSales().first()
        assertTrue(sales.isEmpty())
    }
}

