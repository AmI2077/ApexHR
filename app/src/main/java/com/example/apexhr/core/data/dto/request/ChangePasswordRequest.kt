package com.example.apexhr.core.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Тело POST /api/Auth/change-password. Текущий пароль API не требует. */
@Serializable
data class ChangePasswordRequest(
    @SerialName("newPassword")
    val newPassword: String,
)