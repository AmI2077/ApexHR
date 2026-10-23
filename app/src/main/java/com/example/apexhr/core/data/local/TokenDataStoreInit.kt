package com.example.apexhr.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val TOKENS_DATA_STORE_NAME = "TokenDataStore"

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(TOKENS_DATA_STORE_NAME)

fun getTokenStore(context: Context): DataStore<Preferences> = context.tokenDataStore
