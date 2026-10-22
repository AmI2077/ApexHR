package com.example.apexhr.core.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    @SerialName("accessToken")
    val accessToken: String,

    @SerialName("tokenType")
    val tokenType: String,

    @SerialName("expiresIn")
    val expiresIn: Long,

    @SerialName("refreshToken")
    val refreshToken: String
)