package com.mobile.travelhub.viewmodels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.models.AuthSession
import com.mobile.travelhub.models.LoginRequest
import com.mobile.travelhub.models.RegisterRequest
import com.mobile.travelhub.models.toSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val session: AuthSession? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = authRepository.getSavedSession() != null,
            session = authRepository.getSavedSession()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val normalizedEmail = email.trim()
        val validationError = validateLogin(normalizedEmail, password)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.login(
                LoginRequest(email = normalizedEmail, password = password)
            )

            result.onSuccess { response ->
                authRepository.saveSession(response)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        session = response.toSession(),
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Login failed"
                    )
                }
            }
        }
    }

    fun register(email: String, username: String, password: String) {
        val normalizedEmail = email.trim()
        val normalizedUsername = username.trim()
        val validationError = validateRegister(normalizedEmail, normalizedUsername, password)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.register(
                RegisterRequest(
                    email = normalizedEmail,
                    username = normalizedUsername,
                    password = password
                )
            )

            result.onSuccess { response ->
                authRepository.saveSession(response)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        session = response.toSession(),
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Register failed"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun logout() {
        authRepository.clearSession()
        _uiState.update {
            it.copy(isAuthenticated = false, session = null, errorMessage = null)
        }
    }

    private fun validateLogin(email: String, password: String): String? {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email"
        }

        if (password.length < 8) {
            return "Password must be at least 8 characters"
        }

        return null
    }

    private fun validateRegister(email: String, username: String, password: String): String? {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email"
        }

        if (username.length < 3) {
            return "Username must be at least 3 characters"
        }

        if (password.length < 8) {
            return "Password must be at least 8 characters"
        }

        return null
    }
}
