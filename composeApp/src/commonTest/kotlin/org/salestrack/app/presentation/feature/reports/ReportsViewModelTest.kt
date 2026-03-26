package org.salestrack.app.presentation.feature.reports

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.usecase.reports.GetPeriodReportUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    @Test
    fun should_load_initial_report() = runTest {
        val now = 200_000L
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(
                listOf(
                    Sale("1", "Cafe", "Bebidas", 1, 5_000.0, 0.0, now, "Ana"),
                ),
            ),
            timeProvider = FakeTimeProvider(now),
        )

        val viewModel = ReportsViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            timeProvider = FakeTimeProvider(now),
            repository = repository,
            getPeriodReportUseCase = GetPeriodReportUseCase(repository),
        )

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.report?.summary?.transactionCount)
    }

    @Test
    fun should_change_period_to_custom() = runTest {
        val now = 300_000L
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(
                listOf(Sale("1", "Cafe", "Bebidas", 1, 5_000.0, 0.0, now, "Ana")),
            ),
            timeProvider = FakeTimeProvider(now),
        )

        val viewModel = ReportsViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            timeProvider = FakeTimeProvider(now),
            repository = repository,
            getPeriodReportUseCase = GetPeriodReportUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onEvent(ReportsUiEvent.ChangePeriod(ReportPeriod.Custom))
        viewModel.onEvent(ReportsUiEvent.ChangeCustomRange(now - 5_000L, now + 5_000L))
        advanceUntilIdle()

        assertEquals(ReportPeriod.Custom, viewModel.state.value.selectedPeriod)
        assertEquals(1, viewModel.state.value.report?.transactions?.size)
    }
}

