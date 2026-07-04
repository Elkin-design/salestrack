package org.salestrack.app.presentation.feature.settings

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.usecase.settings.ObserveSettingsUseCase
import org.salestrack.app.domain.usecase.settings.UpdateSettingsUseCase
import org.salestrack.app.domain.usecase.auth.SignOutUseCase
import org.salestrack.app.domain.usecase.auth.GetAuthStateUseCase
import org.salestrack.app.domain.usecase.auth.UpdateDisplayNameUseCase

import org.salestrack.app.domain.repository.SaleRepository

class SettingsViewModel(
    dispatcherProvider: DispatcherProvider,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase = GetAuthStateUseCase(object : org.salestrack.app.domain.repository.AuthRepository {
        override fun observeAuthState() = kotlinx.coroutines.flow.flowOf(null)
        override suspend fun signInWithGoogle(idToken: String) = org.salestrack.app.core.result.AppResult.Failure(Exception())
        override suspend fun signOut() = org.salestrack.app.core.result.AppResult.Success(Unit)
        override fun getCurrentUser() = null
        override suspend fun updateDisplayName(name: String) = org.salestrack.app.core.result.AppResult.Success(Unit)
    }),
    private val signOutUseCase: SignOutUseCase = SignOutUseCase(object : org.salestrack.app.domain.repository.AuthRepository {
        override fun observeAuthState() = kotlinx.coroutines.flow.flowOf(null)
        override suspend fun signInWithGoogle(idToken: String) = org.salestrack.app.core.result.AppResult.Failure(Exception())
        override suspend fun signOut() = org.salestrack.app.core.result.AppResult.Success(Unit)
        override fun getCurrentUser() = null
        override suspend fun updateDisplayName(name: String) = org.salestrack.app.core.result.AppResult.Success(Unit)
    }),
    private val saleRepository: SaleRepository = org.salestrack.app.data.repository.FakeSaleRepository(
        dataSource = org.salestrack.app.data.source.InMemorySaleDataSource(emptyList()),
        timeProvider = org.salestrack.app.core.utils.SystemTimeProvider()
    ),
    private val updateDisplayNameUseCase: UpdateDisplayNameUseCase = UpdateDisplayNameUseCase(object : org.salestrack.app.domain.repository.AuthRepository {
        override fun observeAuthState() = kotlinx.coroutines.flow.flowOf(null)
        override suspend fun signInWithGoogle(idToken: String) = org.salestrack.app.core.result.AppResult.Failure(Exception())
        override suspend fun signOut() = org.salestrack.app.core.result.AppResult.Success(Unit)
        override fun getCurrentUser() = null
        override suspend fun updateDisplayName(name: String) = org.salestrack.app.core.result.AppResult.Success(Unit)
    }),
) : BaseViewModel<SettingsUiState, SettingsUiEvent, SettingsUiEffect>(
    initialState = SettingsUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    init {
        observeSettings()
        observeAuthState()
    }

    override fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.CurrencyChanged -> setState { it.copy(currency = event.value) }
            is SettingsUiEvent.NumberFormatLocaleChanged -> setState { it.copy(numberFormatLocale = event.value) }
            is SettingsUiEvent.TimeZoneChanged -> setState { it.copy(timeZoneId = event.value) }
            is SettingsUiEvent.ThemeModeChanged -> setState { it.copy(themeMode = event.value) }
            is SettingsUiEvent.DesktopFontScaleChanged -> setState { it.copy(desktopFontScale = event.value) }
            SettingsUiEvent.SaveClicked -> saveSettings()
            SettingsUiEvent.SignOutClicked -> signOut()
            SettingsUiEvent.ClearSalesClicked -> clearSales()
            SettingsUiEvent.EditNameClicked -> setState { it.copy(showEditNameDialog = true, editNameInputValue = it.userDisplayName ?: "") }
            SettingsUiEvent.EditNameDismissed -> setState { it.copy(showEditNameDialog = false) }
            is SettingsUiEvent.EditNameValueChanged -> setState { it.copy(editNameInputValue = event.value) }
            SettingsUiEvent.SaveNameClicked -> saveDisplayName()
        }
    }

    private fun signOut() {
        scope.launch {
            signOutUseCase()
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

    private fun saveDisplayName() {
        val newName = state.value.editNameInputValue
        if (newName.isBlank()) return
        scope.launch {
            setState { it.copy(isSaving = true) }
            when (val result = updateDisplayNameUseCase(newName)) {
                is AppResult.Success -> {
                    setState { 
                        it.copy(
                            isSaving = false, 
                            showEditNameDialog = false,
                            userDisplayName = newName 
                        ) 
                    }
                    emitEffect(SettingsUiEffect.ShowMessage("Nombre actualizado"))
                }
                is AppResult.Failure -> {
                    setState {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.error.message ?: "Error al actualizar nombre"
                        )
                    }
                }
            }
        }
    }

    private fun clearSales() {
        scope.launch {
            setState { it.copy(isSaving = true, errorMessage = null) }
            when (val result = saleRepository.clearAllSales()) {
                is AppResult.Success -> {
                    setState { it.copy(isSaving = false) }
                    emitEffect(SettingsUiEffect.ShowMessage("Historial de ventas limpiado"))
                }
                is AppResult.Failure -> {
                    setState {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.error.message ?: "No se pudo limpiar el historial de ventas",
                        )
                    }
                }
            }
        }
    }

    private fun observeAuthState() {
        scope.launch {
            getAuthStateUseCase().collect { user ->
                setState {
                    it.copy(
                        userDisplayName = user?.displayName,
                        userEmail = user?.email,
                        userPhotoUrl = user?.photoUrl
                    )
                }
            }
        }
    }

}
