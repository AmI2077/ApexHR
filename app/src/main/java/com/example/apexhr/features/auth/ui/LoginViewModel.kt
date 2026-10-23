package com.example.apexhr.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.ui.model.AuthState
import com.example.apexhr.features.auth.ui.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(
        AuthState.Checking
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkAuthorization()
    }

    fun checkAuthorization() {
        viewModelScope.launch {
            _authState.value = AuthState.Checking

            try {
                _authState.value = if (authRepository.isAuthorized()) {
                    AuthState.Authorized
                } else {
                    AuthState.Unauthorized
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Не удалось проверить авторизацию"
                )
            }
        }
    }

    fun onLoginChange(value: String) {
        _uiState.update {
            it.copy(
                login = value,
                isError = false
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                isError = false
            )
        }
    }

    fun login() {
        val currentState = _uiState.value

        if (
            currentState.login.isBlank() ||
            currentState.password.isBlank() ||
            currentState.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isError = false
                )
            }

            try {
                val result = authRepository.login(
                    login = currentState.login.trim(),
                    password = currentState.password
                )

                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                login = "",
                                password = "",
                                isLoading = false,
                                isError = false
                            )
                        }

                        _authState.value = AuthState.Authorized
                    },
                    onFailure = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isError = true
                            )
                        }
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _authState.value = AuthState.Unauthorized
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Не удалось выйти из аккаунта"
                )
            }
        }
    }

    fun retryAuthorizationCheck() {
        checkAuthorization()
    }
}