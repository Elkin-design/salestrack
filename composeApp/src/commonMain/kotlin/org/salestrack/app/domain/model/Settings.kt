package org.salestrack.app.domain.model

enum class AppThemeMode {
    System,
    Light,
    Dark,
}

enum class CurrencyCode {
    COP,
    USD,
    EUR,
}

data class AppSettings(
    val currency: CurrencyCode,
    val numberFormatLocale: String,
    val timeZoneId: String,
    val themeMode: AppThemeMode,
    val desktopFontScale: Float,
    val updatedAtMillis: Long,
)
