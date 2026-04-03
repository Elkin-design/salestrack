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

class DeductStockUseCaseTest {

    @Test
    fun should_deduct_stock_when_enough_units() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = DeductStockUseCase(repository)

        val result = useCase(productId = "P-1", quantity = 2)

        assertTrue(result is AppResult.Success)
        assertEquals(10, result.value.stock)
    }

    @Test
    fun should_fail_when_stock_is_insufficient() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = DeductStockUseCase(repository)

        val result = useCase(productId = "P-2", quantity = 50)

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_fail_when_product_is_not_linked_to_inventory() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = DeductStockUseCase(repository)

        val result = useCase(productId = "P-UNKNOWN", quantity = 1)

        assertTrue(result is AppResult.Failure)
    }
}


