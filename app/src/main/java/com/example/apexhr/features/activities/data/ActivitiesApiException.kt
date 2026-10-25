package com.example.apexhr.features.activities.data

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Сервер отклонил действие с активностью.
 * @param serverMessage текст ошибки из тела ответа, если он есть.
 */
class ActivitiesApiException(
    val statusCode: Int,
    val serverMessage: String?,
) : Exception("HTTP $statusCode: $serverMessage")

/** Как requireSuccess, но сохраняет текст ошибки сервера. */
internal suspend fun HttpResponse.requireActionSuccess(): HttpResponse {
    if (!status.isSuccess()) {
        val body = try {
            bodyAsText()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        throw ActivitiesApiException(status.value, body?.let(::parseActivityServerMessage))
    }
    return this
}

/**
 * Достаёт текст ошибки из тела ответа. Бэкенд отвечает `{"message":"…"}`
 * (например, «Survey was not found.»), по Swagger возможен и ProblemDetails
 * `{title, detail, …}`. Не-JSON (например, HTML-страница прокси) игнорируется.
 * Своя копия, а не функция корзины: фичи друг от друга не зависят.
 */
internal fun parseActivityServerMessage(body: String): String? {
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
