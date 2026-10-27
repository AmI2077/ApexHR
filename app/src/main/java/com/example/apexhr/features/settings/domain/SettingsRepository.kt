package com.example.apexhr.features.settings.domain

import kotlinx.coroutines.flow.Flow

/** Настройки приложения, которые переживают перезапуск. */
interface SettingsRepository {
    /**
     * Тёмная тема: true — включена, false — выключена,
     * null — пользователь ещё не выбирал (тогда тема как в системе).
     */
    val darkTheme: Flow<Boolean?>

    suspend fun setDarkTheme(enabled: Boolean)
}

/** Тёмная ли тема сейчас: выбор пользователя, а если его нет — как в системе. */
fun resolveDarkTheme(savedDarkTheme: Boolean?, isSystemInDarkTheme: Boolean): Boolean =
    savedDarkTheme ?: isSystemInDarkTheme
