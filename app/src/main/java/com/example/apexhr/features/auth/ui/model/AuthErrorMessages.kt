package com.example.apexhr.features.auth.ui.model

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.features.auth.domain.AuthError
import com.example.apexhr.features.auth.domain.EmailError
import com.example.apexhr.features.auth.domain.PasswordError

@StringRes
fun AuthError.messageRes(): Int = when (this) {
    AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
    AuthError.Timeout -> R.string.auth_error_timeout
    AuthError.Network -> R.string.auth_error_network
    AuthError.Server -> R.string.auth_error_server
    AuthError.Unknown -> R.string.auth_error_unknown
}

@StringRes
fun EmailError.messageRes(): Int = when (this) {
    EmailError.Empty -> R.string.auth_error_email_empty
    EmailError.InvalidFormat -> R.string.auth_error_email_invalid
}

@StringRes
fun PasswordError.messageRes(): Int = when (this) {
    PasswordError.Empty -> R.string.auth_error_password_empty
}
