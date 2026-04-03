package org.salestrack.app.data.mock

import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.AppSettings
import org.salestrack.app.domain.model.AppThemeMode
import org.salestrack.app.domain.model.CurrencyCode

object MockSettingsFactory {
    fun create(timeProvider: TimeProvider): AppSettings = AppSettings(
        currency = CurrencyCode.COP,
        numberFormatLocale = "es-CO",
        timeZoneId = "America/Bogota",
        themeMode = AppThemeMode.System,
        desktopFontScale = 1.0f,
        updatedAtMillis = timeProvider.nowMillis(),
    )
}
