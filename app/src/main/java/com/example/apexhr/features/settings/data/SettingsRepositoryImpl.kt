package com.example.apexhr.features.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.apexhr.features.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val darkTheme: Flow<Boolean?> =
        dataStore.data
            .map { preferences -> preferences[DARK_THEME_KEY] }
            .distinctUntilChanged()

    override suspend fun setDarkTheme(enabled: Boolean?) {
        dataStore.edit { preferences ->
            if (enabled == null) preferences.remove(DARK_THEME_KEY) else preferences[DARK_THEME_KEY] = enabled
        }
    }

    private companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }
}
