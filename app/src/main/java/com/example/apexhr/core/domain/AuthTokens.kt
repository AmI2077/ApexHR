package com.example.apexhr.core.domain

/**
 * Токены текущей сессии.
 *
 * @param expiresAtMillis момент истечения access-токена (epoch millis),
 * null если сервер не прислал expiresIn.
 * @param mustChangePassword сервер при входе потребовал сменить временный
 * пароль. Хранится вместе с сессией, чтобы после перезапуска приложения
 * снова показать экран смены пароля.
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAtMillis: Long? = null,
    val mustChangePassword: Boolean = false,
)