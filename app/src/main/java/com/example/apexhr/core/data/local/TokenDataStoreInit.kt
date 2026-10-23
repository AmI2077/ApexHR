package com.example.apexhr.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Имя файла DataStore с токенами: files/datastore/TokenDataStore.preferences_pb.
 * Это же имя исключено из резервного копирования в res/xml.
 */
private const val TOKENS_DATA_STORE_NAME = "TokenDataStore"

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(
    name = TOKENS_DATA_STORE_NAME,
    // Повреждённый файл с токенами просто сбрасываем: пользователь войдёт заново.
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

fun getTokenStore(context: Context): DataStore<Preferences> = context.tokenDataStore
