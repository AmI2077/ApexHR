package com.example.apexhr.features.auth.domain

interface AuthRepository {

    suspend fun login(
        email: String,
        password: String,
    ): LoginResult

    suspend fun logout()

    /**
     * Меняет пароль текущего пользователя (POST /api/Auth/change-password).
     * После успеха сессия сохраняется, флаг обязательной смены пароля снимается.
     */
    suspend fun changePassword(newPassword: String): ChangePasswordResult

    /** Проверяет сохранённую сессию локально и через GET /api/Auth/me. */
    suspend fun checkSession(): SessionStatus
}
