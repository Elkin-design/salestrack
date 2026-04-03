package org.salestrack.app.presentation.feature.print

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.usecase.print.PrintReportUseCase

class PrintViewModel(
    dispatcherProvider: DispatcherProvider,
    private val printReportUseCase: PrintReportUseCase,
) : BaseViewModel<PrintUiState, PrintUiEvent, PrintUiEffect>(
    initialState = PrintUiState(),
    dispatcherProvider = dispatcherProvider,
) {
    override fun onEvent(event: PrintUiEvent) {
        when (event) {
            PrintUiEvent.PrintClicked -> print()
        }
    }

    private fun print() {
        scope.launch {
            setState { it.copy(isPrinting = true, errorMessage = null) }
            when (val result = printReportUseCase()) {
                is AppResult.Success -> {
                    setState { it.copy(isPrinting = false, lastResult = "Impresion enviada", errorMessage = null) }
                    emitEffect(PrintUiEffect.ShowMessage("Impresion completada"))
                }
                is AppResult.Failure -> {
                    setState {
                        it.copy(
                            isPrinting = false,
                            errorMessage = result.error.message ?: "No se pudo imprimir",
                        )
                    }
                }
            }
        }
    }
}
