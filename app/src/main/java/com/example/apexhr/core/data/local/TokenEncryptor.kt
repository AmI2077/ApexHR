package com.example.apexhr.core.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

class TokenEncryptor {
    // KeyStore открываем лениво, чтобы не делать это при создании графа Koin
    // на главном потоке.
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getKey(KEY_ALIAS, null)

        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
        )

        return keyGenerator.generateKey()
    }

    /**
     * Удаляет ключ из KeyStore. При следующем шифровании будет создан новый.
     * Нужен, когда старый ключ стал недействителен и расшифровка невозможна.
     */
    fun resetKey() {
        try {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
        } catch (_: GeneralSecurityException) {
            // Ключ уже недоступен, удалять нечего.
        }
    }

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        try {
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        } catch (_: GeneralSecurityException) {
            // Ключ мог стать недействительным: пересоздаём его и пробуем ещё раз.
            resetKey()
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

        val encryptedBytes = cipher.doFinal(
            value.toByteArray(Charsets.UTF_8)
        )

        val iv = cipher.iv

        // Храним IV вместе с зашифрованными данными.
        val combined = iv + encryptedBytes

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    fun decrypt(encryptedValue: String): String {
        val combined = Base64.decode(
            encryptedValue,
            Base64.NO_WRAP
        )

        // Для AES-GCM стандартный размер IV — 12 байт.
        val ivSize = 12

        require(combined.size > ivSize + GCM_TAG_LENGTH / 8) {
            "Invalid encrypted token"
        }

        val iv = combined.copyOfRange(0, ivSize)
        val encryptedBytes = combined.copyOfRange(
            ivSize,
            combined.size
        )

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_LENGTH, iv)
        )

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(
            decryptedBytes,
            Charsets.UTF_8
        )
    }


    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "apexapp_token_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
}