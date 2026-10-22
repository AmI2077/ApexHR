package com.example.apexhr.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.apexhr.core.domain.TokenStorage
import kotlinx.coroutines.flow.first

const val TOKENS_DATA_STORE_NAME = "TokenDataStore"

val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(TOKENS_DATA_STORE_NAME)

class TokenDataStore(
    private val dataStore: DataStore<Preferences>,
    private val encryptor: TokenEncryptor
): TokenStorage {
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        val encryptedAccessToken =
            encryptor.encrypt(accessToken)

        val encryptedRefreshToken =
            encryptor.encrypt(refreshToken)

        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = encryptedAccessToken
            preferences[REFRESH_TOKEN] = encryptedRefreshToken
        }
    }

    override suspend fun getAccessToken(): String? {
        val encryptedToken =
            dataStore.data.first()[ACCESS_TOKEN]
                ?: return null

        return encryptor.decrypt(encryptedToken)
    }

    override suspend fun getRefreshToken(): String? {
        val encryptedToken =
            dataStore.data.first()[REFRESH_TOKEN]
                ?: return null

        return encryptor.decrypt(encryptedToken)
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }

    companion object {
        private val ACCESS_TOKEN =
            stringPreferencesKey("access_token")

        private val REFRESH_TOKEN =
            stringPreferencesKey("refresh_token")
    }
}