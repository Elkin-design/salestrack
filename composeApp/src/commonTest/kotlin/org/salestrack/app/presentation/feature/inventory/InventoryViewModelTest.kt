package org.salestrack.app.presentation.feature.inventory

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.data.repository.FakeInventoryRepository
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.usecase.inventory.AddProductUseCase
import org.salestrack.app.domain.usecase.inventory.AdjustStockUseCase
import org.salestrack.app.domain.usecase.inventory.EditProductUseCase
import org.salestrack.app.domain.usecase.inventory.FilterProductsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    @Test
    fun should_load_products_and_select_first_item() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )

        val viewModel = InventoryViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            repository = repository,
            addProductUseCase = AddProductUseCase(repository),
            editProductUseCase = EditProductUseCase(repository),
            filterProductsUseCase = FilterProductsUseCase(),
            adjustStockUseCase = AdjustStockUseCase(repository),
        )

        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.products.size)
        assertNotNull(viewModel.state.value.selectedProductId)
    }

    @Test
    fun should_filter_products_by_query() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )

        val viewModel = InventoryViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            repository = repository,
            addProductUseCase = AddProductUseCase(repository),
            editProductUseCase = EditProductUseCase(repository),
            filterProductsUseCase = FilterProductsUseCase(),
            adjustStockUseCase = AdjustStockUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onEvent(InventoryUiEvent.QueryChanged("Cafe"))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.products.size)
        assertEquals("Cafe Premium", viewModel.state.value.products.first().name)
    }

    @Test
    fun should_show_error_when_adjustment_has_empty_reason() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )

        val viewModel = InventoryViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            repository = repository,
            addProductUseCase = AddProductUseCase(repository),
            editProductUseCase = EditProductUseCase(repository),
            filterProductsUseCase = FilterProductsUseCase(),
            adjustStockUseCase = AdjustStockUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onEvent(
            InventoryUiEvent.ApplyStockAdjustment(
                productId = "P-1",
                quantityDelta = 1,
                reason = "",
                type = StockAdjustmentType.Entry,
            ),
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.errorMessage != null)
    }
}

