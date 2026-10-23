package com.example.apexhr.features.auth.ui.model

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.features.auth.domain.AuthError

@StringRes
fun AuthError.messageRes(): Int = when (this) {
    AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
    AuthError.Timeout -> R.string.auth_error_timeout
    AuthError.Network -> R.string.auth_error_network
    AuthError.Server -> R.string.auth_error_server
    AuthError.Unknown -> R.string.auth_error_unknown
}
