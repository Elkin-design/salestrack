package org.salestrack.app.presentation.feature.notification

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.usecase.notification.ObserveNotificationSettingsUseCase
import org.salestrack.app.domain.usecase.notification.UpdateNotificationSettingsUseCase

class NotificationSettingsViewModel(
    dispatcherProvider: DispatcherProvider,
    private val observeNotificationSettingsUseCase: ObserveNotificationSettingsUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
) : BaseViewModel<NotificationSettingsUiState, NotificationSettingsUiEvent, NotificationSettingsUiEffect>(
    initialState = NotificationSettingsUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    init {
        observeSettings()
    }

    override fun onEvent(event: NotificationSettingsUiEvent) {
        when (event) {
            is NotificationSettingsUiEvent.DailyReminderEnabledChanged -> {
                setState { it.copy(isDailyReminderEnabled = event.value) }
            }
            is NotificationSettingsUiEvent.ReminderHourChanged -> {
                setState { it.copy(reminderHour24 = event.value.toIntOrNull() ?: it.reminderHour24) }
            }
            is NotificationSettingsUiEvent.ReminderMinuteChanged -> {
                setState { it.copy(reminderMinute = event.value.toIntOrNull() ?: it.reminderMinute) }
            }
            NotificationSettingsUiEvent.SaveClicked -> save()
        }
    }

    private fun observeSettings() {
        scope.launch {
            observeNotificationSettingsUseCase().collect { settings ->
                setState {
                    it.copy(
                        isLoading = false,
                        isDailyReminderEnabled = settings.isDailyReminderEnabled,
                        reminderHour24 = settings.reminderHour24,
                        reminderMinute = settings.reminderMinute,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun save() {
        scope.launch {
            val current = state.value
            setState { it.copy(isSaving = true, errorMessage = null) }

            when (
                val result = updateNotificationSettingsUseCase(
                    isDailyReminderEnabled = current.isDailyReminderEnabled,
                    reminderHour24 = current.reminderHour24,
                    reminderMinute = current.reminderMinute,
                )
            ) {
                is AppResult.Success -> {
                    setState { it.copy(isSaving = false, errorMessage = null) }
                    emitEffect(NotificationSettingsUiEffect.ShowMessage("Notificaciones guardadas"))
                }
                is AppResult.Failure -> {
                    setState {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.error.message ?: "No se pudo guardar configuracion",
                        )
                    }
                }
            }
        }
    }
}
