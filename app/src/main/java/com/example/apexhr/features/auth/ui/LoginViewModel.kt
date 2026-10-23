package com.example.apexhr.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.domain.LoginResult
import com.example.apexhr.features.auth.domain.LoginValidator
import com.example.apexhr.features.auth.ui.model.LoginScreenCallbacks
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
) : ViewModel(), LoginScreenCallbacks {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _actions = Channel<LoginUiActions>(Channel.BUFFERED)

    /** Одноразовые события экрана (успешный вход). */
    val actions: Flow<LoginUiActions> = _actions.receiveAsFlow()

    override fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null,
                error = null
            )
        }
    }

    override fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                error = null
            )
        }
    }

    override fun onLoginClick() {
        val currentState = _uiState.value

        if (currentState.isLoading) {
            return
        }

        val validation = LoginValidator.validate(
            email = currentState.email,
            password = currentState.password
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    emailError = validation.emailError,
                    passwordError = validation.passwordError,
                    error = null
                )
            }
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
                email = currentState.email.trim(),
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
