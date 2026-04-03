package org.salestrack.app.domain.usecase.notification

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.mock.MockNotificationSettingsFactory
import org.salestrack.app.data.repository.FakeNotificationRepository
import org.salestrack.app.data.source.InMemoryNotificationSettingsDataSource
import kotlin.test.Test
import kotlin.test.assertTrue

class UpdateNotificationSettingsUseCaseTest {

    @Test
    fun should_update_notification_settings_when_values_are_valid() = runTest {
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeNotificationRepository(
            dataSource = InMemoryNotificationSettingsDataSource(
                MockNotificationSettingsFactory.create(timeProvider),
            ),
        )
        val useCase = UpdateNotificationSettingsUseCase(repository, timeProvider)

        val result = useCase(
            isDailyReminderEnabled = true,
            reminderHour24 = 9,
            reminderMinute = 30,
        )

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun should_fail_when_hour_is_invalid() = runTest {
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeNotificationRepository(
            dataSource = InMemoryNotificationSettingsDataSource(
                MockNotificationSettingsFactory.create(timeProvider),
            ),
        )
        val useCase = UpdateNotificationSettingsUseCase(repository, timeProvider)

        val result = useCase(
            isDailyReminderEnabled = true,
            reminderHour24 = 25,
            reminderMinute = 30,
        )

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_fail_when_minute_is_invalid() = runTest {
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeNotificationRepository(
            dataSource = InMemoryNotificationSettingsDataSource(
                MockNotificationSettingsFactory.create(timeProvider),
            ),
        )
        val useCase = UpdateNotificationSettingsUseCase(repository, timeProvider)

        val result = useCase(
            isDailyReminderEnabled = true,
            reminderHour24 = 22,
            reminderMinute = 61,
        )

        assertTrue(result is AppResult.Failure)
    }
}
