package org.salestrack.app.presentation.feature.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.mock.MockSettingsFactory
import org.salestrack.app.data.repository.FakeSettingsRepository
import org.salestrack.app.data.source.InMemorySettingsDataSource
import org.salestrack.app.domain.model.AppThemeMode
import org.salestrack.app.domain.model.CurrencyCode
import org.salestrack.app.domain.usecase.settings.ObserveSettingsUseCase
import org.salestrack.app.domain.usecase.settings.UpdateSettingsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @Test
    fun should_load_initial_settings() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeSettingsRepository(
            dataSource = InMemorySettingsDataSource(MockSettingsFactory.create(timeProvider)),
        )

        val viewModel = SettingsViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeSettingsUseCase = ObserveSettingsUseCase(repository),
            updateSettingsUseCase = UpdateSettingsUseCase(repository, timeProvider),
        )

        advanceUntilIdle()

        assertEquals(CurrencyCode.COP, viewModel.state.value.currency)
        assertEquals("America/Bogota", viewModel.state.value.timeZoneId)
        assertTrue(!viewModel.state.value.isLoading)
    }

    @Test
    fun should_persist_changes_after_save() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeSettingsRepository(
            dataSource = InMemorySettingsDataSource(MockSettingsFactory.create(timeProvider)),
        )

        val viewModel = SettingsViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeSettingsUseCase = ObserveSettingsUseCase(repository),
            updateSettingsUseCase = UpdateSettingsUseCase(repository, timeProvider),
        )

        advanceUntilIdle()
        viewModel.onEvent(SettingsUiEvent.CurrencyChanged(CurrencyCode.EUR))
        viewModel.onEvent(SettingsUiEvent.ThemeModeChanged(AppThemeMode.Dark))
        viewModel.onEvent(SettingsUiEvent.SaveClicked)
        advanceUntilIdle()

        assertEquals(CurrencyCode.EUR, viewModel.state.value.currency)
        assertEquals(AppThemeMode.Dark, viewModel.state.value.themeMode)
        assertEquals(null, viewModel.state.value.errorMessage)
    }

    @Test
    fun should_show_error_when_save_is_invalid() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeSettingsRepository(
            dataSource = InMemorySettingsDataSource(MockSettingsFactory.create(timeProvider)),
        )

        val viewModel = SettingsViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeSettingsUseCase = ObserveSettingsUseCase(repository),
            updateSettingsUseCase = UpdateSettingsUseCase(repository, timeProvider),
        )

        advanceUntilIdle()
        viewModel.onEvent(SettingsUiEvent.TimeZoneChanged(""))
        viewModel.onEvent(SettingsUiEvent.SaveClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.errorMessage != null)
    }
}
