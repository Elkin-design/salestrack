package org.salestrack.app.domain.usecase.inventory

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.data.repository.FakeInventoryRepository
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import org.salestrack.app.domain.model.StockAdjustmentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdjustStockUseCaseTest {

    @Test
    fun should_apply_positive_adjustment() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = AdjustStockUseCase(repository)

        val result = useCase(
            productId = "P-1",
            quantityDelta = 3,
            reason = "Compra",
            type = StockAdjustmentType.Entry,
        )

        assertTrue(result is AppResult.Success)
        assertEquals(15, result.value.stock)
    }

    @Test
    fun should_clamp_negative_adjustment_to_zero() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = AdjustStockUseCase(repository)

        val result = useCase(
            productId = "P-2",
            quantityDelta = -100,
            reason = "Perdida",
            type = StockAdjustmentType.Loss,
        )

        assertTrue(result is AppResult.Success)
        assertEquals(0, result.value.stock)
    }

    @Test
    fun should_fail_when_reason_is_blank() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = AdjustStockUseCase(repository)

        val result = useCase(
            productId = "P-1",
            quantityDelta = 2,
            reason = "",
            type = StockAdjustmentType.Entry,
        )

        assertTrue(result is AppResult.Failure)
    }
}


