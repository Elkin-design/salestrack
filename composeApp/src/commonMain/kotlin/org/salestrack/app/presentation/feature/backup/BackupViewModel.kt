package org.salestrack.app.presentation.feature.backup

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.usecase.backup.CreateBackupUseCase

class BackupViewModel(
    dispatcherProvider: DispatcherProvider,
    private val createBackupUseCase: CreateBackupUseCase,
) : BaseViewModel<BackupUiState, BackupUiEvent, BackupUiEffect>(
    initialState = BackupUiState(),
    dispatcherProvider = dispatcherProvider,
) {
    override fun onEvent(event: BackupUiEvent) {
        when (event) {
            BackupUiEvent.RunBackup -> runBackup()
        }
    }

    private fun runBackup() {
        scope.launch {
            setState { it.copy(isRunning = true, errorMessage = null) }
            when (val result = createBackupUseCase()) {
                is AppResult.Success -> {
                    setState {
                        it.copy(
                            isRunning = false,
                            lastResult = "${result.value.fileName} | ${result.value.jsonPreview}",
                            errorMessage = null,
                        )
                    }
                    emitEffect(BackupUiEffect.ShowMessage("Backup completado"))
                }
                is AppResult.Failure -> {
                    setState {
                        it.copy(
                            isRunning = false,
                            errorMessage = result.error.message ?: "No se pudo generar backup",
                        )
                    }
                }
            }
        }
    }
}
