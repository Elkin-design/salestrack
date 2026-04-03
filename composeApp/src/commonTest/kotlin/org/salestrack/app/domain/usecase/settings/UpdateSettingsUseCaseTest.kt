package org.salestrack.app.domain.usecase.settings

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.repository.FakeSettingsRepository
import org.salestrack.app.data.source.InMemorySettingsDataSource
import org.salestrack.app.domain.model.AppThemeMode
import org.salestrack.app.domain.model.CurrencyCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateSettingsUseCaseTest {

    @Test
    fun should_update_settings_with_valid_values() = runTest {
        val timeProvider = FakeTimeProvider(9_000L)
        val repository = FakeSettingsRepository(
            dataSource = InMemorySettingsDataSource(
                org.salestrack.app.data.mock.MockSettingsFactory.create(timeProvider),
            ),
        )
        val useCase = UpdateSettingsUseCase(repository, timeProvider)

        val result = useCase(
            currency = CurrencyCode.USD,
            numberFormatLocale = "en-US",
            timeZoneId = "America/New_York",
            themeMode = AppThemeMode.Dark,
            desktopFontScale = 1.2f,
        )

        assertTrue(result is AppResult.Success)
        assertEquals(CurrencyCode.USD, result.value.currency)
        assertEquals("America/New_York", result.value.timeZoneId)
    }

    @Test
    fun should_fail_when_timezone_is_blank() = runTest {
        val timeProvider = FakeTimeProvider(9_000L)
        val repository = FakeSettingsRepository(
            dataSource = InMemorySettingsDataSource(
                org.salestrack.app.data.mock.MockSettingsFactory.create(timeProvider),
            ),
        )
        val useCase = UpdateSettingsUseCase(repository, timeProvider)

        val result = useCase(
            currency = CurrencyCode.COP,
            numberFormatLocale = "es-CO",
            timeZoneId = "",
            themeMode = AppThemeMode.System,
            desktopFontScale = 1.0f,
        )

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_fail_when_font_scale_is_out_of_range() = runTest {
        val timeProvider = FakeTimeProvider(9_000L)
        val repository = FakeSettingsRepository(
            dataSource = InMemorySettingsDataSource(
                org.salestrack.app.data.mock.MockSettingsFactory.create(timeProvider),
            ),
        )
        val useCase = UpdateSettingsUseCase(repository, timeProvider)

        val result = useCase(
            currency = CurrencyCode.COP,
            numberFormatLocale = "es-CO",
            timeZoneId = "America/Bogota",
            themeMode = AppThemeMode.System,
            desktopFontScale = 2.5f,
        )

        assertTrue(result is AppResult.Failure)
    }
}
