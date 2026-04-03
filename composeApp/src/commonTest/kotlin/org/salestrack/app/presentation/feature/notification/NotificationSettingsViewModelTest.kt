package org.salestrack.app.presentation.feature.notification

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.mock.MockNotificationSettingsFactory
import org.salestrack.app.data.repository.FakeNotificationRepository
import org.salestrack.app.data.source.InMemoryNotificationSettingsDataSource
import org.salestrack.app.domain.usecase.notification.ObserveNotificationSettingsUseCase
import org.salestrack.app.domain.usecase.notification.UpdateNotificationSettingsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationSettingsViewModelTest {

    @Test
    fun should_load_initial_notification_settings() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeNotificationRepository(
            dataSource = InMemoryNotificationSettingsDataSource(
                MockNotificationSettingsFactory.create(timeProvider),
            ),
        )

        val viewModel = NotificationSettingsViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeNotificationSettingsUseCase = ObserveNotificationSettingsUseCase(repository),
            updateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(repository, timeProvider),
        )

        advanceUntilIdle()

        assertTrue(!viewModel.state.value.isLoading)
        assertEquals(20, viewModel.state.value.reminderHour24)
    }

    @Test
    fun should_save_notification_settings_after_changes() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeNotificationRepository(
            dataSource = InMemoryNotificationSettingsDataSource(
                MockNotificationSettingsFactory.create(timeProvider),
            ),
        )

        val viewModel = NotificationSettingsViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeNotificationSettingsUseCase = ObserveNotificationSettingsUseCase(repository),
            updateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(repository, timeProvider),
        )

        advanceUntilIdle()
        viewModel.onEvent(NotificationSettingsUiEvent.ReminderHourChanged("7"))
        viewModel.onEvent(NotificationSettingsUiEvent.ReminderMinuteChanged("15"))
        viewModel.onEvent(NotificationSettingsUiEvent.SaveClicked)
        advanceUntilIdle()

        assertEquals(7, viewModel.state.value.reminderHour24)
        assertEquals(15, viewModel.state.value.reminderMinute)
        assertEquals(null, viewModel.state.value.errorMessage)
    }

    @Test
    fun should_show_error_when_save_has_invalid_values() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeNotificationRepository(
            dataSource = InMemoryNotificationSettingsDataSource(
                MockNotificationSettingsFactory.create(timeProvider),
            ),
        )

        val viewModel = NotificationSettingsViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeNotificationSettingsUseCase = ObserveNotificationSettingsUseCase(repository),
            updateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(repository, timeProvider),
        )

        advanceUntilIdle()
        viewModel.onEvent(NotificationSettingsUiEvent.ReminderHourChanged("99"))
        viewModel.onEvent(NotificationSettingsUiEvent.SaveClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.errorMessage != null)
    }
}
