package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.source.InMemorySettingsDataSource
import org.salestrack.app.domain.model.AppSettings
import org.salestrack.app.domain.repository.SettingsRepository

class FakeSettingsRepository(
    private val dataSource: InMemorySettingsDataSource,
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = dataSource.observe()

    override suspend fun updateSettings(settings: AppSettings): AppResult<AppSettings> {
        dataSource.update(settings)
        return AppResult.Success(settings)
    }
}
