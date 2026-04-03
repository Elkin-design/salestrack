package org.salestrack.app.domain.usecase.settings

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.AppSettings
import org.salestrack.app.domain.model.AppThemeMode
import org.salestrack.app.domain.model.CurrencyCode
import org.salestrack.app.domain.repository.SettingsRepository

class UpdateSettingsUseCase(
    private val repository: SettingsRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        currency: CurrencyCode,
        numberFormatLocale: String,
        timeZoneId: String,
        themeMode: AppThemeMode,
        desktopFontScale: Float,
    ): AppResult<AppSettings> {
        if (numberFormatLocale.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El locale numerico es obligatorio"))
        }
        if (timeZoneId.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("La zona horaria es obligatoria"))
        }
        if (desktopFontScale < 0.8f || desktopFontScale > 2.0f) {
            return AppResult.Failure(IllegalArgumentException("El tamano de fuente debe estar entre 0.8 y 2.0"))
        }

        return repository.updateSettings(
            AppSettings(
                currency = currency,
                numberFormatLocale = numberFormatLocale,
                timeZoneId = timeZoneId,
                themeMode = themeMode,
                desktopFontScale = desktopFontScale,
                updatedAtMillis = timeProvider.nowMillis(),
            ),
        )
    }
}
