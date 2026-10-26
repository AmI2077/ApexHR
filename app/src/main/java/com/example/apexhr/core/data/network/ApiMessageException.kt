package com.example.apexhr.core.data.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Сервер ответил кодом, отличным от 2xx, и, возможно, объяснил причину.
 * @param serverMessage текст ошибки из тела ответа, если он есть.
 */
class ApiMessageException(
    val statusCode: Int,
    val serverMessage: String?,
) : Exception("HTTP $statusCode")

/** Как [requireSuccess], но сохраняет текст ошибки сервера. */
suspend fun HttpResponse.requireSuccessWithMessage(): HttpResponse {
    if (!status.isSuccess()) {
        val body = try {
            bodyAsText()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        throw ApiMessageException(status.value, body?.let(::parseServerErrorMessage))
    }
    return this
}

/**
 * Достаёт текст ошибки из тела ответа: бэкенд отвечает 400 `{"message":"…"}`,
 * ASP.NET может вернуть ProblemDetails `{title, detail, …}`.
 * Не-JSON (например, HTML-страница прокси) игнорируется.
 */
fun parseServerErrorMessage(body: String): String? {
    val json = try {
        Json.parseToJsonElement(body) as? JsonObject
    } catch (e: Exception) {
        null
    } ?: return null

    return listOf("message", "detail", "title")
        .firstNotNullOfOrNull { key ->
            (json[key] as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.content
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }
}