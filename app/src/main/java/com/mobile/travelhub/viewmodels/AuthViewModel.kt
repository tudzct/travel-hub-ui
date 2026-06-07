package com.mobile.travelhub.viewmodels

import android.util.Patterns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.DeviceTokenRepository
import com.mobile.travelhub.data.model.AuthSession
import com.mobile.travelhub.data.model.LoginRequest
import com.mobile.travelhub.data.model.RegisterRequest
import com.mobile.travelhub.data.model.toSession
import com.mobile.travelhub.data.userMessage
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
    val isOnboarded: Boolean = false,
    val session: AuthSession? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val deviceTokenRepository: DeviceTokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = authRepository.getSavedSession() != null,
            isOnboarded = authRepository.getSavedSession()?.isOnboarded == true,
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
                val onboardedResponse = response.copy(isOnboarded = true)
                authRepository.saveSession(onboardedResponse)
                registerDeviceToken()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        isOnboarded = true,
                        session = onboardedResponse.toSession(),
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.userMessage("Đăng nhập không thành công")
                    )
                }
            }
        }
    }

    fun register(email: String, username: String, name: String, password: String) {
        val normalizedEmail = email.trim()
        val normalizedUsername = username.trim()
        val normalizedName = name.trim()
        val validationError = validateRegister(normalizedEmail, normalizedUsername, normalizedName, password)
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
                    name = normalizedName,
                    password = password
                )
            )

            result.onSuccess { response ->
                val onboardedResponse = response.copy(isOnboarded = true)
                authRepository.saveSession(onboardedResponse)
                registerDeviceToken()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        isOnboarded = true,
                        session = onboardedResponse.toSession(),
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.userMessage("Đăng ký không thành công")
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
            return "Vui lòng nhập email hợp lệ"
        }

        if (password.length < 8) {
            return "Mật khẩu phải có ít nhất 8 ký tự"
        }

        return null
    }

    private fun validateRegister(email: String, username: String, name: String, password: String): String? {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Vui lòng nhập email hợp lệ"
        }

        if (username.length < 3) {
            return "Tên người dùng phải có ít nhất 3 ký tự"
        }

        if (name.length < 2) {
            return "Tên hiển thị phải có ít nhất 2 ký tự"
        }

        if (password.length < 8) {
            return "Mật khẩu phải có ít nhất 8 ký tự"
        }

        return null
    }

    private suspend fun registerDeviceToken() {
        deviceTokenRepository.registerCurrentDeviceToken()
            .onFailure { throwable ->
                Log.w(TAG, "Không thể đăng ký token thiết bị", throwable)
            }
    }

    private companion object {
        private const val TAG = "AuthViewModel"
    }
}
