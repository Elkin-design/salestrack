package com.salestrack.presentation.viewmodel

import com.salestrack.domain.model.User
import com.salestrack.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : BaseViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _authState.value = if (user != null) AuthState.Authenticated(user) else AuthState.Idle
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, password)
                .onSuccess { user -> _authState.value = AuthState.Authenticated(user) }
                .onFailure { error -> _authState.value = AuthState.Error(error.message ?: "Unknown error") }
        }
    }

    fun register(email: String, password: String, name: String, businessId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.register(email, password, name, businessId)
                .onSuccess { user -> _authState.value = AuthState.Authenticated(user) }
                .onFailure { error -> _authState.value = AuthState.Error(error.message ?: "Unknown error") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Idle
        }
    }
}
