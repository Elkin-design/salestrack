package org.salestrack.app.presentation.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.GoogleSignInNavigator
import org.salestrack.app.domain.usecase.auth.GetAuthStateUseCase
import org.salestrack.app.domain.usecase.auth.SignInWithGoogleUseCase
import org.salestrack.app.domain.usecase.auth.SignOutUseCase

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val googleSignInNavigator: GoogleSignInNavigator
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collect { user ->
                _uiState.update { it.copy(isAuthenticated = user != null) }
            }
        }
    }

    fun signInWithGoogle() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        googleSignInNavigator.signIn { idToken, error ->
            if (idToken != null) {
                performFirebaseLogin(idToken)
            } else {
                _uiState.update { it.copy(isLoading = false, error = error ?: "Cancelado por el usuario") }
            }
        }
    }

    private fun performFirebaseLogin(idToken: String) {
        viewModelScope.launch {
            when (val result = signInWithGoogleUseCase(idToken)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }
}
