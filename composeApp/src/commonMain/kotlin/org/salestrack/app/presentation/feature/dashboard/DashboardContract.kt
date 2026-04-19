package org.salestrack.app.presentation.feature.dashboard

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.DashboardSummary
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.model.ReportPeriod

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary = DashboardSummary(
        totalSoldToday = 0.0,
        transactionCountToday = 0,
        topProductToday = "Sin ventas",
        syncStatus = "Sincronizado",
    ),
    val recentSales: List<Sale> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val weeklyTrend: List<DashboardTrendPoint> = emptyList(),
    val categoryBreakdown: List<DashboardCategoryShare> = emptyList(),
    val errorMessage: String? = null,
    val showExportModal: Boolean = false,
) : UiState

data class DashboardTrendPoint(
    val label: String,
    val amount: Double,
)

data class DashboardCategoryShare(
    val category: String,
    val amount: Double,
)

sealed interface DashboardUiEvent : UiEvent {
    data object Refresh : DashboardUiEvent
    data class NavigateToReports(val period: ReportPeriod) : DashboardUiEvent
    data object NavigateToExport : DashboardUiEvent
    data class ToggleExportModal(val show: Boolean) : DashboardUiEvent
}

sealed interface DashboardUiEffect : UiEffect {
    data class ShowMessage(val message: String, val isSuccess: Boolean = true) : DashboardUiEffect
    sealed interface NavigateToDestination : DashboardUiEffect {
        val destination: org.salestrack.app.presentation.app.AppDestination
        
        data class Default(override val destination: org.salestrack.app.presentation.app.AppDestination) : NavigateToDestination
        data class NavigateToReportsWithPeriod(
            override val destination: org.salestrack.app.presentation.app.AppDestination,
            val period: ReportPeriod
        ) : NavigateToDestination
    }
}

