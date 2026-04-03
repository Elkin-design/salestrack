package org.salestrack.app.data.source

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.salestrack.app.domain.model.NotificationSettings

class InMemoryNotificationSettingsDataSource(initial: NotificationSettings) {
    private val state = MutableStateFlow(initial)

    fun observe(): StateFlow<NotificationSettings> = state.asStateFlow()

    fun update(value: NotificationSettings) {
        state.value = value
    }
}
