package org.salestrack.app.presentation.feature.export

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ExportFormat
import org.salestrack.app.domain.usecase.export.ExportCsvUseCase
import org.salestrack.app.domain.usecase.export.ExportExcelUseCase
import org.salestrack.app.domain.usecase.export.ExportPdfUseCase

class ExportReportViewModel(
    dispatcherProvider: DispatcherProvider,
    private val exportPdfUseCase: ExportPdfUseCase,
    private val exportExcelUseCase: ExportExcelUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
) : BaseViewModel<ExportReportUiState, ExportReportUiEvent, ExportReportUiEffect>(
    initialState = ExportReportUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    override fun onEvent(event: ExportReportUiEvent) {
        when (event) {
            is ExportReportUiEvent.FormatChanged -> setState { it.copy(selectedFormat = event.value) }
            is ExportReportUiEvent.DestinationChanged -> setState { it.copy(selectedDestination = event.value) }
            is ExportReportUiEvent.IncludeSellerColumnChanged -> setState { it.copy(includeSellerColumn = event.value) }
            ExportReportUiEvent.ExportClicked -> export()
        }
    }

    private fun export() {
        scope.launch {
            val current = state.value
            setState { it.copy(isExporting = true, errorMessage = null) }

            val result = when (current.selectedFormat) {
                ExportFormat.Pdf -> exportPdfUseCase(
                    destination = current.selectedDestination,
                    includeSellerColumn = current.includeSellerColumn,
                )
                ExportFormat.Excel -> exportExcelUseCase(
                    destination = current.selectedDestination,
                    includeSellerColumn = current.includeSellerColumn,
                )
                ExportFormat.Csv -> exportCsvUseCase(
                    destination = current.selectedDestination,
                    includeSellerColumn = current.includeSellerColumn,
                )
            }

            when (result) {
                is AppResult.Success -> {
                    setState {
                        it.copy(
                            isExporting = false,
                            lastResult = "${result.value.fileName} | ${result.value.destination}",
                            errorMessage = null,
                        )
                    }
                    emitEffect(ExportReportUiEffect.ShowMessage("Exportacion completada"))
                }
                is AppResult.Failure -> {
                    setState {
                        it.copy(
                            isExporting = false,
                            errorMessage = result.error.message ?: "No se pudo exportar",
                        )
                    }
                }
            }
        }
    }
}
