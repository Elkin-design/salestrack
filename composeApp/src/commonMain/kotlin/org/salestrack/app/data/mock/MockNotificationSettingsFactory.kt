package org.salestrack.app.data.mock

import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.NotificationSettings

object MockNotificationSettingsFactory {
    fun create(timeProvider: TimeProvider): NotificationSettings = NotificationSettings(
        isDailyReminderEnabled = true,
        reminderHour24 = 20,
        reminderMinute = 0,
        updatedAtMillis = timeProvider.nowMillis(),
    )
}
