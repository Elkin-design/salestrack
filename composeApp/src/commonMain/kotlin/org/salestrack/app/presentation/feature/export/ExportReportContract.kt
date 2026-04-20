package org.salestrack.app.presentation.feature.export

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.ExportDestination
import org.salestrack.app.domain.model.ExportFormat

data class ExportReportUiState(
    val isExporting: Boolean = false,
    val selectedFormat: ExportFormat = ExportFormat.Pdf,
    val selectedDestination: ExportDestination = ExportDestination.SaveLocal,
    val includeSellerColumn: Boolean = false,
    val lastResult: String? = null,
    val savedArtifact: org.salestrack.app.domain.model.ExportArtifact? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface ExportReportUiEvent : UiEvent {
    data class FormatChanged(val value: ExportFormat) : ExportReportUiEvent
    data class DestinationChanged(val value: ExportDestination) : ExportReportUiEvent
    data class IncludeSellerColumnChanged(val value: Boolean) : ExportReportUiEvent
    data object ExportClicked : ExportReportUiEvent
    data object OpenSavedFile : ExportReportUiEvent
}

sealed interface ExportReportUiEffect : UiEffect {
    data class ShowMessage(val message: String) : ExportReportUiEffect
}
