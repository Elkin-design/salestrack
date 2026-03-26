package org.salestrack.app.presentation.feature.reports

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.ReportData
import org.salestrack.app.domain.model.ReportPeriod

data class ReportsUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: ReportPeriod = ReportPeriod.Daily,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val customFromMillis: Long = 0L,
    val customToMillis: Long = 0L,
    val report: ReportData? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface ReportsUiEvent : UiEvent {
    data object Refresh : ReportsUiEvent
    data class ChangePeriod(val period: ReportPeriod) : ReportsUiEvent
    data class ChangeCategory(val category: String?) : ReportsUiEvent
    data class ChangeCustomRange(val fromMillis: Long, val toMillis: Long) : ReportsUiEvent
}

sealed interface ReportsUiEffect : UiEffect {
    data class ShowMessage(val message: String) : ReportsUiEffect
}

