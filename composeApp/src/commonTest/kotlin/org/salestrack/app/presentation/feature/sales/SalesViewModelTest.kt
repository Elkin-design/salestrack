package org.salestrack.app.presentation.feature.sales

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.usecase.sales.AddSaleUseCase
import org.salestrack.app.domain.usecase.sales.DeleteSaleUseCase
import org.salestrack.app.domain.usecase.sales.FilterSalesUseCase
import org.salestrack.app.domain.usecase.sales.UpdateSaleUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModelTest {

    @Test
    fun should_filter_sales_by_query() = runTest {
        val now = 10_000L
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(
                listOf(
                    Sale(
                        id = "1",
                        productName = "Cafe",
                        category = "Bebidas",
                        quantity = 1,
                        unitPrice = 5_000.0,
                        discount = 0.0,
                        createdAtMillis = now,
                        sellerName = "Ana",
                    ),
                    Sale(
                        id = "2",
                        productName = "Galletas",
                        category = "Snacks",
                        quantity = 1,
                        unitPrice = 3_000.0,
                        discount = 0.0,
                        createdAtMillis = now,
                        sellerName = "Luis",
                    ),
                ),
            ),
            timeProvider = FakeTimeProvider(now),
        )

        val viewModel = SalesViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            repository = repository,
            addSaleUseCase = AddSaleUseCase(repository),
            updateSaleUseCase = UpdateSaleUseCase(repository),
            deleteSaleUseCase = DeleteSaleUseCase(repository),
            filterSalesUseCase = FilterSalesUseCase(),
        )

        advanceUntilIdle()
        viewModel.onEvent(SalesUiEvent.QueryChanged("Cafe"))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.sales.size)
        assertEquals("Cafe", viewModel.state.value.sales.first().productName)
    }

    @Test
    fun should_add_new_sale() = runTest {
        val now = 20_000L
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(emptyList()),
            timeProvider = FakeTimeProvider(now),
        )

        val viewModel = SalesViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            repository = repository,
            addSaleUseCase = AddSaleUseCase(repository),
            updateSaleUseCase = UpdateSaleUseCase(repository),
            deleteSaleUseCase = DeleteSaleUseCase(repository),
            filterSalesUseCase = FilterSalesUseCase(),
        )

        advanceUntilIdle()
        viewModel.onEvent(
            SalesUiEvent.SaveNewSale(
                productName = "Te Helado",
                category = "Bebidas",
                quantity = 2,
                unitPrice = 4_500.0,
                discount = 0.0,
                seller = "Ana",
            ),
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.sales.size)
        assertEquals("Te Helado", viewModel.state.value.sales.first().productName)
    }
}

