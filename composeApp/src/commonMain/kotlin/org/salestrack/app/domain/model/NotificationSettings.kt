package org.salestrack.app.domain.model

data class NotificationSettings(
    val isDailyReminderEnabled: Boolean,
    val reminderHour24: Int,
    val reminderMinute: Int,
    val updatedAtMillis: Long,
)
