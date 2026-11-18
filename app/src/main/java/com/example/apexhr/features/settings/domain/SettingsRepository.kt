package com.example.apexhr.features.settings.domain

import kotlinx.coroutines.flow.Flow

/** Настройки приложения, которые переживают перезапуск. */
interface SettingsRepository {
    /**
     * Тёмная тема: true — включена, false — выключена,
     * null — пользователь ещё не выбирал (тогда тема как в системе).
     */
    val darkTheme: Flow<Boolean?>

    /** @param enabled null — сбросить выбор и следовать системной теме. */
    suspend fun setDarkTheme(enabled: Boolean?)
}

/** Выбор темы, как в вебе: «Как в системе» (по умолчанию), «Светлая», «Тёмная». */
enum class ThemeChoice { System, Light, Dark }

fun Boolean?.toThemeChoice(): ThemeChoice = when (this) {
    null -> ThemeChoice.System
    true -> ThemeChoice.Dark
    false -> ThemeChoice.Light
}

/** Что сохранить в настройках: null — как в системе. */
fun ThemeChoice.toSavedDarkTheme(): Boolean? = when (this) {
    ThemeChoice.System -> null
    ThemeChoice.Light -> false
    ThemeChoice.Dark -> true
}

/** Тёмная ли тема сейчас: выбор пользователя, а если его нет — как в системе. */
fun resolveDarkTheme(savedDarkTheme: Boolean?, isSystemInDarkTheme: Boolean): Boolean =
    savedDarkTheme ?: isSystemInDarkTheme
