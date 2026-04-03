package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.source.InMemoryNotificationSettingsDataSource
import org.salestrack.app.domain.model.NotificationSettings
import org.salestrack.app.domain.repository.NotificationRepository

class FakeNotificationRepository(
    private val dataSource: InMemoryNotificationSettingsDataSource,
) : NotificationRepository {

    override fun observeSettings(): Flow<NotificationSettings> = dataSource.observe()

    override suspend fun updateSettings(settings: NotificationSettings): AppResult<NotificationSettings> {
        dataSource.update(settings)
        return AppResult.Success(settings)
    }
}
