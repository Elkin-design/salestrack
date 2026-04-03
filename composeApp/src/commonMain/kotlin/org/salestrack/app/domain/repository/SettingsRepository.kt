package org.salestrack.app.domain.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.AppSettings

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun updateSettings(settings: AppSettings): AppResult<AppSettings>
}
