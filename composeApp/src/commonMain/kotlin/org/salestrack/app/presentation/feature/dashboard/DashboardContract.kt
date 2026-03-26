package org.salestrack.app.presentation.feature.dashboard

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.DashboardSummary
import org.salestrack.app.domain.model.Sale

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary = DashboardSummary(
        totalSoldToday = 0.0,
        transactionCountToday = 0,
        topProductToday = "Sin ventas",
        syncStatus = "Sincronizado",
    ),
    val recentSales: List<Sale> = emptyList(),
    val errorMessage: String? = null,
) : UiState

sealed interface DashboardUiEvent : UiEvent {
    data object Refresh : DashboardUiEvent
}

sealed interface DashboardUiEffect : UiEffect {
    data class ShowMessage(val message: String) : DashboardUiEffect
}

