package org.salestrack.app.presentation.feature.dashboard

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.usecase.dashboard.BuildDashboardSummaryUseCase
import org.salestrack.app.domain.usecase.sales.FilterSalesUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @Test
    fun should_load_summary_and_recent_sales() = runTest {
        val now = 2_000_000L
        val repo = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(
                listOf(
                    Sale(
                        id = "1",
                        productName = "A",
                        category = "Bebidas",
                        quantity = 2,
                        unitPrice = 5.0,
                        discount = 0.0,
                        createdAtMillis = now,
                        sellerName = "Ana",
                    ),
                ),
            ),
            timeProvider = FakeTimeProvider(now),
        )

        val vm = DashboardViewModel(
            dispatcherProvider = FakeDispatcherProvider(StandardTestDispatcher(testScheduler)),
            repository = repo,
            timeProvider = FakeTimeProvider(now),
            buildSummary = BuildDashboardSummaryUseCase(),
            filterSalesUseCase = FilterSalesUseCase(),
        )

        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.summary.transactionCountToday)
        assertEquals(1, state.recentSales.size)
    }
}


