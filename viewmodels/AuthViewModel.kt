package com.example.jobsterra.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobsterra.data.models.AuthState
import com.example.jobsterra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val currentUser = authRepository.getCurrentUser()
                _authState.value = if (currentUser != null) {
                    AuthState.Authenticated(currentUser)
                } else {
                    AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            val result = authRepository.iniciarSesion(email.trim(), password)

            result.fold(
                onSuccess = { usuario ->
                    _authState.value = AuthState.Authenticated(usuario)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Error desconocido")
                }
            )

            _isLoading.value = false
        }
    }

    fun signUp(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            val result = authRepository.registrarUsuario(email.trim(), password, nombre.trim())

            result.fold(
                onSuccess = { usuario ->
                    _authState.value = AuthState.Authenticated(usuario)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Error desconocido")
                }
            )

            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.cerrarSesion()
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al cerrar sesión")
            }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}