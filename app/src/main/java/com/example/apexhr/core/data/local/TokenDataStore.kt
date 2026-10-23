package com.example.apexhr.core.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.apexhr.core.domain.AuthTokens
import com.example.apexhr.core.domain.TokenStorage
import kotlinx.coroutines.flow.first
import java.security.GeneralSecurityException
import java.security.ProviderException

class TokenDataStore(
    private val dataStore: DataStore<Preferences>,
    private val encryptor: TokenEncryptor
): TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens) {
        val encryptedAccessToken =
            encryptor.encrypt(tokens.accessToken)

        val encryptedRefreshToken =
            tokens.refreshToken?.let(encryptor::encrypt)

        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = encryptedAccessToken

            if (encryptedRefreshToken != null) {
                preferences[REFRESH_TOKEN] = encryptedRefreshToken
            } else {
                preferences.remove(REFRESH_TOKEN)
            }

            if (tokens.expiresAtMillis != null) {
                preferences[EXPIRES_AT] = tokens.expiresAtMillis
            } else {
                preferences.remove(EXPIRES_AT)
            }
        }
    }

    override suspend fun getTokens(): AuthTokens? {
        val preferences = dataStore.data.first()

        val encryptedAccessToken = preferences[ACCESS_TOKEN] ?: return null

        return try {
            AuthTokens(
                accessToken = encryptor.decrypt(encryptedAccessToken),
                refreshToken = preferences[REFRESH_TOKEN]?.let(encryptor::decrypt),
                expiresAtMillis = preferences[EXPIRES_AT]
            )
        } catch (e: GeneralSecurityException) {
            onDecryptionFailed(e)
        } catch (e: ProviderException) {
            // Ошибка самого AndroidKeyStore (RuntimeException).
            onDecryptionFailed(e)
        } catch (e: IllegalArgumentException) {
            // Битый Base64 или слишком короткие данные.
            onDecryptionFailed(e)
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(EXPIRES_AT)
        }
    }

    private suspend fun onDecryptionFailed(error: Exception): AuthTokens? {
        // Сам токен в лог не пишем, только тип ошибки.
        Log.w(LOG_TAG, "Не удалось расшифровать токены: ${error.javaClass.simpleName}")
        clearTokens()
        encryptor.resetKey()
        return null
    }

    companion object {
        private const val LOG_TAG = "TokenDataStore"

        private val ACCESS_TOKEN =
            stringPreferencesKey("access_token")

        private val REFRESH_TOKEN =
            stringPreferencesKey("refresh_token")

        private val EXPIRES_AT =
            longPreferencesKey("expires_at")
    }
}
