package org.salestrack.app.domain.usecase.notification

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.domain.model.NotificationSettings
import org.salestrack.app.domain.repository.NotificationRepository

class ObserveNotificationSettingsUseCase(
    private val repository: NotificationRepository,
) {
    operator fun invoke(): Flow<NotificationSettings> = repository.observeSettings()
}
