package org.salestrack.app.data.source

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.salestrack.app.domain.model.AppSettings

class InMemorySettingsDataSource(initialSettings: AppSettings) {
    private val settingsState = MutableStateFlow(initialSettings)

    fun observe(): StateFlow<AppSettings> = settingsState.asStateFlow()

    fun update(settings: AppSettings) {
        settingsState.value = settings
    }
}
