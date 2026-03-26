package org.salestrack.app.presentation.feature.dashboard

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.usecase.dashboard.BuildDashboardSummaryUseCase
import org.salestrack.app.domain.usecase.sales.FilterSalesUseCase
import org.salestrack.app.core.utils.TimeProvider

class DashboardViewModel(
    dispatcherProvider: DispatcherProvider,
    private val repository: SaleRepository,
    private val timeProvider: TimeProvider,
    private val buildSummary: BuildDashboardSummaryUseCase,
    private val filterSalesUseCase: FilterSalesUseCase,
) : BaseViewModel<DashboardUiState, DashboardUiEvent, DashboardUiEffect>(
    initialState = DashboardUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    init {
        observeSales()
    }

    override fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.Refresh -> setState { it.copy(isLoading = true, errorMessage = null) }
        }
    }

    private fun observeSales() {
        scope.launch {
            repository.observeSales().collect { sales ->
                render(sales)
            }
        }
    }

    private fun render(sales: List<Sale>) {
        val summary = buildSummary(sales, timeProvider.nowMillis())
        val recent = filterSalesUseCase(
            sales = sales,
            query = "",
            category = null,
        ).take(5)

        setState {
            it.copy(
                isLoading = false,
                summary = summary,
                recentSales = recent,
                errorMessage = null,
            )
        }
    }
}

