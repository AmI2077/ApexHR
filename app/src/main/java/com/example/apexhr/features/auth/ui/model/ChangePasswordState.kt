package com.example.apexhr.features.auth.ui.model

import com.example.apexhr.features.auth.domain.ChangePasswordError
import com.example.apexhr.features.auth.domain.ConfirmPasswordError
import com.example.apexhr.features.auth.domain.NewPasswordError

data class ChangePasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    /** Ошибка ответа сервера или сети. */
    val error: ChangePasswordError? = null,
    val newPasswordError: NewPasswordError? = null,
    val confirmPasswordError: ConfirmPasswordError? = null,
)

sealed interface ChangePasswordUiActions {
    /** Пароль сменён, можно пускать в приложение. */
    data object Success : ChangePasswordUiActions

    /** Пользователь решил выйти, не меняя пароль. */
    data object Logout : ChangePasswordUiActions
}

interface ChangePasswordScreenCallbacks {
    fun onNewPasswordChange(value: String)
    fun onConfirmPasswordChange(value: String)
    fun onSubmitClick()
    fun onLogoutClick()

    companion object {
        val Empty = object : ChangePasswordScreenCallbacks {
            override fun onNewPasswordChange(value: String) = Unit
            override fun onConfirmPasswordChange(value: String) = Unit
            override fun onSubmitClick() = Unit
            override fun onLogoutClick() = Unit
        }
    }
}