package com.example.apexhr.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.domain.LoginResult
import com.example.apexhr.features.auth.ui.model.LoginUiActions
import com.example.apexhr.features.auth.ui.model.LoginUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _actions = Channel<LoginUiActions>(Channel.BUFFERED)

    /** Одноразовые события экрана (успешный вход). */
    val actions: Flow<LoginUiActions> = _actions.receiveAsFlow()

    fun onLoginChange(value: String) {
        _uiState.update {
            it.copy(
                login = value,
                error = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                error = null
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
                    error = null
                )
            }

            val result = authRepository.login(
                email = currentState.login.trim(),
                password = currentState.password
            )

            when (result) {
                LoginResult.Success -> {
                    _uiState.value = LoginUiState()
                    _actions.send(LoginUiActions.Success)
                }

                is LoginResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error
                        )
                    }
                }
            }
        }
    }
}
