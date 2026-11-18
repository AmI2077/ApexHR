package com.example.apexhr.core.testing

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * Клиент на движке OkHttp без сети: запоминает запросы и отвечает кодом [code]
 * с JSON-телом [body] и заголовком Content-Type, чтобы Ktor мог разобрать ответ.
 */
class JsonRecordingHttpClient(code: Int = 200, body: String = "{}") {
    val requests = mutableListOf<Request>()

    val client = HttpClient(OkHttp) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        engine {
            addInterceptor { chain ->
                requests += chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("HTTP $code")
                    .header("Content-Type", "application/json")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }
    }
}

/** Тело запроса в виде строки UTF-8. */
fun Request.bodyText(): String = Buffer().also { body!!.writeTo(it) }.readUtf8()