package com.example.apexhr.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.domain.ChangePasswordError
import com.example.apexhr.features.auth.domain.ChangePasswordResult
import com.example.apexhr.features.auth.domain.ChangePasswordValidator
import com.example.apexhr.features.auth.ui.model.ChangePasswordScreenCallbacks
import com.example.apexhr.features.auth.ui.model.ChangePasswordUiActions
import com.example.apexhr.features.auth.ui.model.ChangePasswordUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Обязательная смена временного пароля при первом входе. */
class ChangePasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel(), ChangePasswordScreenCallbacks {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _actions = Channel<ChangePasswordUiActions>(Channel.BUFFERED)

    /** Одноразовые события экрана (пароль сменён, выход). */
    val actions: Flow<ChangePasswordUiActions> = _actions.receiveAsFlow()

    override fun onNewPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                newPassword = value,
                newPasswordError = null,
                error = null
            )
        }
    }

    override fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                confirmPasswordError = null,
                error = null
            )
        }
    }

    override fun onSubmitClick() {
        val currentState = _uiState.value

        if (currentState.isLoading) {
            return
        }

        val validation = ChangePasswordValidator.validate(
            newPassword = currentState.newPassword,
            confirmPassword = currentState.confirmPassword
        )

        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    newPasswordError = validation.newPasswordError,
                    confirmPasswordError = validation.confirmPasswordError,
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

            when (val result = authRepository.changePassword(currentState.newPassword)) {
                ChangePasswordResult.Success -> {
                    _uiState.value = ChangePasswordUiState()
                    _actions.send(ChangePasswordUiActions.Success)
                }

                is ChangePasswordResult.Failure -> {
                    if (result.error == ChangePasswordError.SessionExpired) {
                        // На экран входа переведёт SessionEventBus,
                        // введённые пароли не храним.
                        _uiState.value = ChangePasswordUiState()
                    } else {
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

    override fun onLogoutClick() {
        if (_uiState.value.isLoading) {
            return
        }

        _uiState.value = ChangePasswordUiState()
        viewModelScope.launch {
            _actions.send(ChangePasswordUiActions.Logout)
        }
    }
}