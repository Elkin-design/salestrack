package org.salestrack.app.domain.usecase.inventory

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.data.repository.FakeInventoryRepository
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetLowStockProductsUseCaseTest {

    @Test
    fun should_return_products_below_or_equal_to_threshold() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = GetLowStockProductsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(1, result.value.size)
        assertEquals("P-2", result.value.first().id)
    }

    @Test
    fun should_include_product_at_exact_threshold() = runTest {
        val products = MockInventoryFactory.create().map {
            if (it.id == "P-1") it.copy(stock = it.minimumStock) else it
        }
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(products),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = GetLowStockProductsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertTrue(result.value.any { it.id == "P-1" })
    }

    @Test
    fun should_return_empty_when_stock_is_sufficient() = runTest {
        val products = MockInventoryFactory.create().map { it.copy(stock = it.minimumStock + 10) }
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(products),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = GetLowStockProductsUseCase(repository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertTrue(result.value.isEmpty())
    }
}
