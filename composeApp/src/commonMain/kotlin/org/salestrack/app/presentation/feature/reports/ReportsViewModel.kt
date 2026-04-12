package org.salestrack.app.presentation.feature.reports

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.usecase.reports.GetPeriodReportUseCase

class ReportsViewModel(
    dispatcherProvider: DispatcherProvider,
    private val timeProvider: TimeProvider,
    private val repository: SaleRepository,
    private val getPeriodReportUseCase: GetPeriodReportUseCase,
    initialPeriod: ReportPeriod,
) : BaseViewModel<ReportsUiState, ReportsUiEvent, ReportsUiEffect>(
    initialState = ReportsUiState(selectedPeriod = initialPeriod),
    dispatcherProvider = dispatcherProvider,
) {

    private var latestSales: List<Sale> = emptyList()

    init {
        val now = timeProvider.nowMillis()
        setState { it.copy(customFromMillis = now - DEFAULT_CUSTOM_RANGE, customToMillis = now) }
        observeSales()
    }

    override fun onEvent(event: ReportsUiEvent) {
        when (event) {
            ReportsUiEvent.Refresh -> loadReport()
            is ReportsUiEvent.ChangePeriod -> {
                setState { it.copy(selectedPeriod = event.period) }
                loadReport()
            }
            is ReportsUiEvent.ChangeCategory -> {
                setState { it.copy(selectedCategory = event.category) }
                loadReport()
            }
            is ReportsUiEvent.ChangeCustomRange -> {
                setState { it.copy(customFromMillis = event.fromMillis, customToMillis = event.toMillis) }
                if (state.value.selectedPeriod == ReportPeriod.Custom) {
                    loadReport()
                }
            }
        }
    }

    private fun observeSales() {
        scope.launch {
            repository.observeSales().collect { sales ->
                latestSales = sales
                val categories = sales.map { it.category }.distinct().sorted()
                setState { it.copy(categories = categories, isLoading = false) }
                loadReport()
            }
        }
    }

    private fun loadReport() {
        scope.launch {
            val current = state.value
            val result = getPeriodReportUseCase(
                period = current.selectedPeriod,
                nowMillis = timeProvider.nowMillis(),
                fromMillis = current.customFromMillis,
                toMillis = current.customToMillis,
                category = current.selectedCategory,
            )

            when (result) {
                is AppResult.Success -> setState {
                    it.copy(
                        isLoading = false,
                        report = result.value,
                        errorMessage = null,
                    )
                }
                is AppResult.Failure -> setState {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.error.message ?: "Error cargando reportes",
                    )
                }
            }
        }
    }

    private companion object {
        const val DEFAULT_CUSTOM_RANGE = 7L * 24L * 60L * 60L * 1000L
    }
}

