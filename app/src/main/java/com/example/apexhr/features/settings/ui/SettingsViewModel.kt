package com.example.apexhr.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.features.settings.domain.SettingsRepository
import com.example.apexhr.features.settings.ui.models.SettingsScreenCallbacks
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Настройки: пока только тёмная тема. «Назад» и выход из аккаунта
 * задаёт навигация (переопределяются в AppNavigation).
 */
class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel(), SettingsScreenCallbacks {

    /** Сохранённый выбор тёмной темы; null — не выбран (тема как в системе). */
    val darkTheme: StateFlow<Boolean?> = repository.darkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override fun onBackClick() = Unit

    override fun onDarkThemeChange(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }
}
