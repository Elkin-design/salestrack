package org.salestrack.app.domain.usecase.notification

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.NotificationSettings
import org.salestrack.app.domain.repository.NotificationRepository

class UpdateNotificationSettingsUseCase(
    private val repository: NotificationRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        isDailyReminderEnabled: Boolean,
        reminderHour24: Int,
        reminderMinute: Int,
    ): AppResult<NotificationSettings> {
        if (reminderHour24 !in 0..23) {
            return AppResult.Failure(IllegalArgumentException("La hora debe estar entre 0 y 23"))
        }
        if (reminderMinute !in 0..59) {
            return AppResult.Failure(IllegalArgumentException("Los minutos deben estar entre 0 y 59"))
        }

        return repository.updateSettings(
            NotificationSettings(
                isDailyReminderEnabled = isDailyReminderEnabled,
                reminderHour24 = reminderHour24,
                reminderMinute = reminderMinute,
                updatedAtMillis = timeProvider.nowMillis(),
            ),
        )
    }
}
