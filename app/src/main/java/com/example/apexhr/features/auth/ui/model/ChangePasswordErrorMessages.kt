package com.example.apexhr.features.auth.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.apexhr.R
import com.example.apexhr.features.auth.domain.ChangePasswordError
import com.example.apexhr.features.auth.domain.ChangePasswordValidator
import com.example.apexhr.features.auth.domain.ConfirmPasswordError
import com.example.apexhr.features.auth.domain.NewPasswordError

/** Текст ошибки: если сервер объяснил причину, показываем его текст. */
@Composable
fun ChangePasswordError.message(): String = when (this) {
    is ChangePasswordError.Rejected ->
        serverMessage ?: stringResource(R.string.change_password_error_rejected)

    ChangePasswordError.SessionExpired -> stringResource(R.string.auth_session_expired)
    is ChangePasswordError.Auth -> stringResource(error.messageRes())
}

@Composable
fun NewPasswordError.message(): String = when (this) {
    NewPasswordError.Empty -> stringResource(R.string.change_password_error_new_empty)
    NewPasswordError.TooShort -> stringResource(
        R.string.change_password_error_too_short,
        ChangePasswordValidator.MIN_PASSWORD_LENGTH
    )
}

@StringRes
fun ConfirmPasswordError.messageRes(): Int = when (this) {
    ConfirmPasswordError.Empty -> R.string.change_password_error_confirm_empty
    ConfirmPasswordError.Mismatch -> R.string.change_password_error_mismatch
}