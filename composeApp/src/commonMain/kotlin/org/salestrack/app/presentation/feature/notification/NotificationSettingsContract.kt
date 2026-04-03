package org.salestrack.app.presentation.feature.notification

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState

data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val isDailyReminderEnabled: Boolean = true,
    val reminderHour24: Int = 20,
    val reminderMinute: Int = 0,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface NotificationSettingsUiEvent : UiEvent {
    data class DailyReminderEnabledChanged(val value: Boolean) : NotificationSettingsUiEvent
    data class ReminderHourChanged(val value: String) : NotificationSettingsUiEvent
    data class ReminderMinuteChanged(val value: String) : NotificationSettingsUiEvent
    data object SaveClicked : NotificationSettingsUiEvent
}

sealed interface NotificationSettingsUiEffect : UiEffect {
    data class ShowMessage(val message: String) : NotificationSettingsUiEffect
}
