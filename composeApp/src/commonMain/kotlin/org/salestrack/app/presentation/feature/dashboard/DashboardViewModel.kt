package org.salestrack.app.presentation.feature.dashboard

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.usecase.dashboard.BuildDashboardSummaryUseCase
import org.salestrack.app.domain.usecase.sales.FilterSalesUseCase
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.usecase.inventory.GetLowStockProductsUseCase
import org.salestrack.app.presentation.app.AppDestination

class DashboardViewModel(
    dispatcherProvider: DispatcherProvider,
    private val repository: SaleRepository,
    private val timeProvider: TimeProvider,
    private val buildSummary: BuildDashboardSummaryUseCase,
    private val filterSalesUseCase: FilterSalesUseCase,
    private val getLowStockProducts: GetLowStockProductsUseCase,
) : BaseViewModel<DashboardUiState, DashboardUiEvent, DashboardUiEffect>(
    initialState = DashboardUiState(),
    dispatcherProvider = dispatcherProvider,
) {
    private var lastSales: List<Sale> = emptyList()
    private var lastLowStock: List<Product> = emptyList()
    private val oneDayMillis = 86_400_000L

    init {
        observeSales()
        fetchLowStock()
    }

    override fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.Refresh -> {
                setState { it.copy(isLoading = true, errorMessage = null) }
                fetchLowStock()
                render(lastSales)
                emitEffect(DashboardUiEffect.ShowMessage("Dashboard actualizado"))
            }

            is DashboardUiEvent.NavigateToReports -> {
                emitEffect(DashboardUiEffect.NavigateToDestination.NavigateToReportsWithPeriod(
                    destination = AppDestination.Reports,
                    period = event.period
                ))
            }

            DashboardUiEvent.NavigateToExport -> {
                setState { it.copy(showExportModal = true) }
            }

            is DashboardUiEvent.ToggleExportModal -> {
                setState { it.copy(showExportModal = event.show) }
            }
        }
    }

    private fun observeSales() {
        scope.launch {
            runCatching {
                repository.observeSales().collect { sales ->
                    lastSales = sales
                    render(sales)
                }
            }.onFailure { throwable ->
                setState {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Error inesperado",
                    )
                }
                emitEffect(DashboardUiEffect.ShowMessage("No se pudo actualizar"))
            }
        }
    }

    private fun fetchLowStock() {
        scope.launch {
            val result = getLowStockProducts()
            if (result is AppResult.Success) {
                lastLowStock = result.value
                setState { it.copy(lowStockProducts = lastLowStock) }
            }
        }
    }

    private fun render(sales: List<Sale>) {
        val nowMillis = timeProvider.nowMillis()
        val summary = buildSummary(sales, nowMillis)
        val recent = filterSalesUseCase(
            sales = sales,
            query = "",
            category = null,
        ).take(5)
        val weeklyTrend = buildWeeklyTrend(sales, nowMillis)
        val categoryBreakdown = buildCategoryBreakdown(sales)

        setState {
            it.copy(
                isLoading = false,
                summary = summary,
                recentSales = recent,
                weeklyTrend = weeklyTrend,
                categoryBreakdown = categoryBreakdown,
                errorMessage = null,
            )
        }
    }

    private fun buildWeeklyTrend(
        sales: List<Sale>,
        nowMillis: Long,
    ): List<DashboardTrendPoint> {
        return (6 downTo 0).map { offset ->
            val dayStart = nowMillis - (offset * oneDayMillis)
            val dayEnd = dayStart + oneDayMillis
            val amount = sales
                .asSequence()
                .filter { it.createdAtMillis in dayStart until dayEnd }
                .sumOf { it.netTotal }
            val label = if (offset == 0) "Hoy" else "D-${offset}"

            DashboardTrendPoint(label = label, amount = amount)
        }
    }

    private fun buildCategoryBreakdown(sales: List<Sale>): List<DashboardCategoryShare> {
        return sales
            .groupBy { sale -> sale.category.ifBlank { "Sin categoria" } }
            .map { (category, values) ->
                DashboardCategoryShare(
                    category = category,
                    amount = values.sumOf { it.netTotal },
                )
            }
            .sortedByDescending { it.amount }
            .take(4)
    }
}

