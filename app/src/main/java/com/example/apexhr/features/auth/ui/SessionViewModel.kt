package com.example.apexhr.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.SessionEventBus
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.domain.SessionStatus
import com.example.apexhr.features.auth.ui.model.AuthState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Отвечает за состояние сессии всего приложения: проверка при старте,
 * переход после входа, обязательная смена пароля, выход и реакция на 401
 * от сервера.
 */
class SessionViewModel(
    private val authRepository: AuthRepository,
    sessionEventBus: SessionEventBus,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var checkJob: Job? = null

    init {
        viewModelScope.launch {
            sessionEventBus.sessionExpired.collect {
                val current = _authState.value
                if (
                    current is AuthState.Authorized ||
                    current is AuthState.Checking ||
                    current is AuthState.PasswordChangeRequired
                ) {
                    checkJob?.cancel()
                    _authState.value = AuthState.Unauthorized(isSessionExpired = true)
                }
            }
        }

        checkSession()
    }

    fun checkSession() {
        checkJob?.cancel()
        checkJob = viewModelScope.launch {
            _authState.value = AuthState.Checking

            _authState.value = when (val status = authRepository.checkSession()) {
                SessionStatus.Active -> AuthState.Authorized
                SessionStatus.PasswordChangeRequired -> AuthState.PasswordChangeRequired
                SessionStatus.NotAuthorized -> AuthState.Unauthorized()
                SessionStatus.Expired -> AuthState.Unauthorized(isSessionExpired = true)
                is SessionStatus.CheckFailed -> AuthState.Error(status.error)
            }
        }
    }

    fun onLoginSuccess() {
        checkJob?.cancel()
        _authState.value = AuthState.Authorized
    }

    fun onPasswordChangeRequired() {
        checkJob?.cancel()
        _authState.value = AuthState.PasswordChangeRequired
    }

    /** Пароль сменён: токен прежний, пускаем в приложение без перелогина. */
    fun onPasswordChanged() {
        checkJob?.cancel()
        _authState.value = AuthState.Authorized
    }

    fun logout() {
        checkJob?.cancel()
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Unauthorized()
        }
    }
}
