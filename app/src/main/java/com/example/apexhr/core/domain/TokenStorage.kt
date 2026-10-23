package com.example.apexhr.core.domain

interface TokenStorage {

    suspend fun saveTokens(tokens: AuthTokens)

    /**
     * Возвращает сохранённые токены или null, если их нет.
     * Если токены не удалось расшифровать (сброс ключа KeyStore, восстановление
     * на другом устройстве и т.п.), хранилище очищается и тоже возвращается null.
     */
    suspend fun getTokens(): AuthTokens?

    suspend fun getAccessToken(): String? = getTokens()?.accessToken

    suspend fun clearTokens()
}
