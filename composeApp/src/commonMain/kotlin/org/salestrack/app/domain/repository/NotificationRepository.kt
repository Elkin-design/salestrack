package org.salestrack.app.domain.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NotificationSettings

interface NotificationRepository {
    fun observeSettings(): Flow<NotificationSettings>
    suspend fun updateSettings(settings: NotificationSettings): AppResult<NotificationSettings>
}
