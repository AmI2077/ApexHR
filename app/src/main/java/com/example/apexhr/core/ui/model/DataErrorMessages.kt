package com.example.apexhr.core.ui.model

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError

@StringRes
fun DataError.messageRes(): Int = when (this) {
    DataError.Unauthorized -> R.string.auth_session_expired
    DataError.Timeout -> R.string.data_error_timeout
    DataError.Network -> R.string.data_error_network
    DataError.Server -> R.string.data_error_server
    DataError.Unknown -> R.string.data_error_unknown
}
