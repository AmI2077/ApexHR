package com.example.apexhr.features.profile.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

/**
 * Поле sub из полезной нагрузки JWT (id пользователя Supabase) или null,
 * если токен не похож на JWT. Подпись не проверяется: нужен только id
 * для пути файла, права всё равно проверит хранилище.
 */
fun jwtSubject(token: String): String? = try {
    val payload = token.split('.').takeIf { it.size == 3 }?.get(1) ?: return null
    val json = Base64.getUrlDecoder().decode(payload.trimEnd('=')).toString(Charsets.UTF_8)
    Json.parseToJsonElement(json).jsonObject["sub"]?.jsonPrimitive?.contentOrNull
        ?.takeIf { it.isNotBlank() }
} catch (e: IllegalArgumentException) {
    // Битый base64 или JSON (SerializationException — её подкласс), либо sub не строка.
    null
}
