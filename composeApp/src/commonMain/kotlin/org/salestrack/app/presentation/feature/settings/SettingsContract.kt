package org.salestrack.app.presentation.feature.settings

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.AppThemeMode
import org.salestrack.app.domain.model.CurrencyCode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val currency: CurrencyCode = CurrencyCode.COP,
    val numberFormatLocale: String = "es-CO",
    val timeZoneId: String = "America/Bogota",
    val themeMode: AppThemeMode = AppThemeMode.System,
    val desktopFontScale: Float = 1.0f,
    val errorMessage: String? = null,
) : UiState

sealed interface SettingsUiEvent : UiEvent {
    data class CurrencyChanged(val value: CurrencyCode) : SettingsUiEvent
    data class NumberFormatLocaleChanged(val value: String) : SettingsUiEvent
    data class TimeZoneChanged(val value: String) : SettingsUiEvent
    data class ThemeModeChanged(val value: AppThemeMode) : SettingsUiEvent
    data class DesktopFontScaleChanged(val value: Float) : SettingsUiEvent
    data object SaveClicked : SettingsUiEvent
}

sealed interface SettingsUiEffect : UiEffect {
    data class ShowMessage(val message: String) : SettingsUiEffect
}
