package com.example.apexhr.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.ui.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState =
        MutableStateFlow(LoginUiState())

    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
//        viewModelScope.launch {
//            _loginState.value = LoginUiState.Loading
//
//            authRepository.login(email, password)
//                .onSuccess {
//                    _loginState.value = LoginUiState.Success
//                }
//                .onFailure { error ->
//                    _loginState.value = LoginUiState.Error(
//                        error.message ?: "Не удалось войти"
//                    )
//                }
//        }
    }
}