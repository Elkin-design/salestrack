package org.salestrack.app.presentation.feature.print

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState

data class PrintUiState(
    val isPrinting: Boolean = false,
    val lastResult: String? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface PrintUiEvent : UiEvent {
    data object PrintClicked : PrintUiEvent
}

sealed interface PrintUiEffect : UiEffect {
    data class ShowMessage(val message: String) : PrintUiEffect
}
