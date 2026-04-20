package org.salestrack.app.presentation.feature.settings

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.usecase.settings.ObserveSettingsUseCase
import org.salestrack.app.domain.usecase.settings.PopulateSampleDataUseCase
import org.salestrack.app.domain.usecase.settings.UpdateSettingsUseCase

class SettingsViewModel(
    dispatcherProvider: DispatcherProvider,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val populateSampleDataUseCase: PopulateSampleDataUseCase,
) : BaseViewModel<SettingsUiState, SettingsUiEvent, SettingsUiEffect>(
    initialState = SettingsUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    init {
        observeSettings()
    }

    override fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.CurrencyChanged -> setState { it.copy(currency = event.value) }
            is SettingsUiEvent.NumberFormatLocaleChanged -> setState { it.copy(numberFormatLocale = event.value) }
            is SettingsUiEvent.TimeZoneChanged -> setState { it.copy(timeZoneId = event.value) }
            is SettingsUiEvent.ThemeModeChanged -> setState { it.copy(themeMode = event.value) }
            is SettingsUiEvent.DesktopFontScaleChanged -> setState { it.copy(desktopFontScale = event.value) }
            SettingsUiEvent.SaveClicked -> saveSettings()
            SettingsUiEvent.GenerateSampleDataClicked -> generateSampleData()
        }
    }

    private fun observeSettings() {
        scope.launch {
            observeSettingsUseCase().collect { settings ->
                setState {
                    it.copy(
                        isLoading = false,
                        currency = settings.currency,
                        numberFormatLocale = settings.numberFormatLocale,
                        timeZoneId = settings.timeZoneId,
                        themeMode = settings.themeMode,
                        desktopFontScale = settings.desktopFontScale,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun saveSettings() {
        scope.launch {
            val current = state.value
            setState { it.copy(isSaving = true, errorMessage = null) }

            when (
                val result = updateSettingsUseCase(
                    currency = current.currency,
                    numberFormatLocale = current.numberFormatLocale,
                    timeZoneId = current.timeZoneId,
                    themeMode = current.themeMode,
                    desktopFontScale = current.desktopFontScale,
                )
            ) {
                is AppResult.Success -> {
                    setState { it.copy(isSaving = false, errorMessage = null) }
                    emitEffect(SettingsUiEffect.ShowMessage("Configuracion guardada"))
                }
                is AppResult.Failure -> {
                    setState {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.error.message ?: "No se pudo guardar configuracion",
                        )
                    }
                }
            }
        }
    }

    private fun generateSampleData() {
        scope.launch {
            setState { it.copy(isGeneratingData = true, errorMessage = null) }
            when (val result = populateSampleDataUseCase.execute()) {
                is AppResult.Success -> {
                    setState { it.copy(isGeneratingData = false) }
                    emitEffect(SettingsUiEffect.ShowMessage("Datos de restaurante generados exitosamente"))
                }
                is AppResult.Failure -> {
                    setState {
                        it.copy(
                            isGeneratingData = false,
                            errorMessage = result.error.message ?: "Error al generar datos de prueba",
                        )
                    }
                }
            }
        }
    }
}
