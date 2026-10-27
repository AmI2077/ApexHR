package com.example.apexhr.features.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore

/** Имя файла DataStore с настройками: files/datastore/SettingsDataStore.preferences_pb. */
private const val SETTINGS_DATA_STORE_NAME = "SettingsDataStore"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATA_STORE_NAME,
    // Повреждённый файл настроек сбрасываем: вернутся значения по умолчанию.
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

fun getSettingsStore(context: Context): DataStore<Preferences> = context.settingsDataStore
