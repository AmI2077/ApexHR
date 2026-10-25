package com.example.apexhr.features.shop.data

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Сервер отклонил действие с корзиной или заказом.
 * @param serverMessage текст ошибки из тела ответа, если он есть.
 */
class CartApiException(
    val statusCode: Int,
    val serverMessage: String?,
) : Exception("HTTP $statusCode: $serverMessage")

/**
 * Как requireSuccess, но сохраняет текст ошибки: по нему отличаем
 * «нет на складе» от «не хватает баланса».
 */
internal suspend fun HttpResponse.requireActionSuccess(): HttpResponse {
    if (!status.isSuccess()) {
        val body = try {
            bodyAsText()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        throw CartApiException(status.value, body?.let(::parseServerMessage))
    }
    return this
}

/**
 * Достаёт текст ошибки из тела ответа. Бэкенд корзины отвечает
 * `{"message":"…"}`, по Swagger возможен и ProblemDetails `{title, detail, …}`.
 * Не-JSON (например, HTML-страница прокси) игнорируется.
 */
internal fun parseServerMessage(body: String): String? {
    val json = try {
        Json.parseToJsonElement(body) as? JsonObject
    } catch (e: Exception) {
        null
    } ?: return null

    return listOf("detail", "message", "title")
        .firstNotNullOfOrNull { key ->
            (json[key] as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.content
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }
}
