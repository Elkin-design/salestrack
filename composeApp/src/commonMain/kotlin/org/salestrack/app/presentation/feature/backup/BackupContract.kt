package org.salestrack.app.presentation.feature.backup

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState

data class BackupUiState(
    val isRunning: Boolean = false,
    val lastResult: String? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface BackupUiEvent : UiEvent {
    data object RunBackup : BackupUiEvent
}

sealed interface BackupUiEffect : UiEffect {
    data class ShowMessage(val message: String) : BackupUiEffect
}
