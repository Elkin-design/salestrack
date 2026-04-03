package org.salestrack.app.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.domain.model.AppSettings
import org.salestrack.app.domain.repository.SettingsRepository

class ObserveSettingsUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = repository.observeSettings()
}
