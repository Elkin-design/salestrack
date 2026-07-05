package org.salestrack.app.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.salestrack.app.domain.repository.RemoteConfigRepository

data class AppUiState(
    val forceUpdateRequired: Boolean = false,
    val isChecking: Boolean = true
)

class AppViewModel(
    private val remoteConfigRepository: RemoteConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        checkRemoteConfig()
    }

    private fun checkRemoteConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true)
            val success = remoteConfigRepository.fetchAndActivate()
            
            val forceUpdate = if (success) {
                remoteConfigRepository.getBoolean("force_update_required")
            } else {
                false // Fallback si no hay internet: Permitir entrar
            }

            _uiState.value = _uiState.value.copy(
                forceUpdateRequired = forceUpdate,
                isChecking = false
            )
        }
    }
}
